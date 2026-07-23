import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import request from '../../utils/request'
import Layout from '../../components/layout/Layout'
import { useToast } from '../../context/ToastContext'
import type { Category } from '../../types'

export default function PublishProduct() {
  const navigate = useNavigate()
  const { toast } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [contactInfo, setContactInfo] = useState('')
  const [images, setImages] = useState<File[]>([])
  const [previews, setPreviews] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    request.get('/categories/').then((res: any) => {
      const nextCategories = Array.isArray(res?.data) ? res.data : []
      setCategories(Array.isArray(nextCategories) ? nextCategories : [])
    }).catch(() => {})
  }, [])

  function handleImageChange(e: ChangeEvent<HTMLInputElement>) {
    const files = e.target.files
    if (!files) return
    const newFiles = Array.from(files).slice(0, 3 - images.length)
    const updated = [...images, ...newFiles].slice(0, 3)
    setImages(updated)
    setPreviews(updated.map(f => URL.createObjectURL(f)))
    e.target.value = ''
  }

  function removeImage(index: number) {
    const updated = images.filter((_, i) => i !== index)
    setImages(updated)
    setPreviews(updated.map(f => URL.createObjectURL(f)))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    if (!categoryId) { setError('请选择适合的交易品类'); return }
    const priceNum = parseFloat(price)
    if (isNaN(priceNum) || priceNum <= 0) { setError('物品售价必须是大于0的数值'); return }

    setLoading(true)
    try {
      const b64Pics: string[] = []
      for (let i = 0; i < images.length; i++) {
        const base64 = await toBase64(images[i])
        b64Pics.push(base64)
      }

      await request.post('/products/', {
        title,
        description,
        price,
        category: categoryId,
        contact_info: contactInfo,
        uploaded_images: b64Pics
      })
      toast('宝贝发布成功，请等待系统管理员审核批准！', 'success')
      navigate('/profile', { replace: true })
    } catch (e: any) {
      setError(e?.message || '商品发布失败')
    } finally {
      setLoading(false)
    }
  }

  const toBase64 = (file: File): Promise<string> => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = error => reject(error);
  });

  return (
    <Layout>
      <div className="max-w-xl mx-auto">
        <div className="mb-6 select-none text-left">
          <h1 className="text-xl font-bold tracking-tight text-neutral-900">
            发布物品
          </h1>
          <p className="text-xs text-neutral-500 mt-1 font-semibold leading-relaxed">
            请提供您想要置换闲置的好物详情。信息越精确，在校流转体验越快捷。
          </p>
        </div>

        <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-primary-faint p-6 space-y-5 select-none">

          <div>
            <label className="block text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-1.5">
              物品简短标题 *
            </label>
            <input
              type="text"
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
              maxLength={100}
              placeholder="如：考研政治考点清单 9.9成新 无任何涂画"
              className="w-full px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 placeholder:text-neutral-500"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-1.5">
              详细闲置描述 *
            </label>
            <textarea
              value={description}
              onChange={e => setDescription(e.target.value)}
              required
              maxLength={2000}
              rows={4}
              placeholder="写明宝贝的入手渠道、使用时长、瑕疵问题以及为什么想要出售等，真实信息更有助于快速面交。"
              className="w-full px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 placeholder:text-neutral-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-1.5">
                理想售价 (元) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={price}
                onChange={e => setPrice(e.target.value)}
                required
                placeholder="0.00"
                className="w-full px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 placeholder:text-neutral-500"
              />
            </div>
            <div>
              <label className="block text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-1.5">
                商品品类筛选 *
              </label>
              <select
                value={categoryId}
                onChange={e => setCategoryId(e.target.value)}
                required
                className="w-full px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 bg-white"
              >
                <option value="">请选择商品分类板块</option>
                {categories.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-1.5">
              线下当面交易联系信息 *
            </label>
            <input
              type="text"
              value={contactInfo}
              onChange={e => setContactInfo(e.target.value)}
              required
              placeholder="请设定微信号、手机号。购买者成功发起预约后，才能在详情页查看"
              className="w-full px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 placeholder:text-neutral-500"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-1.5">
              宝贝照片（最多 3 张）
            </label>
            <div className="flex gap-3 flex-wrap items-center mt-2.5">
              {previews.map((src, i) => (
                <div key={i} className="relative w-20 h-20 rounded-xl overflow-hidden border border-primary-faint">
                  <img src={src} className="w-full h-full object-cover pointer-events-none" />
                  <button
                    type="button"
                    onClick={() => removeImage(i)}
                    className="absolute top-1 right-1 bg-primary hover:bg-primary-dark text-white rounded-md w-5 h-5 text-[9px] flex items-center justify-center font-bold cursor-pointer transition-colors shadow-xs"
                  >
                    ✕
                  </button>
                </div>
              ))}
              {images.length < 3 && (
                <label className="w-20 h-20 border-2 border-dashed border-primary-faint rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-primary hover:bg-primary-subtle transition-all">
                  <span className="text-neutral-500 text-lg font-light">+</span>
                  <span className="text-[9px] text-neutral-500 tracking-wider font-bold">拍摄 / 选择</span>
                  <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
                </label>
              )}
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-xl p-3 flex items-start gap-2.5 select-none text-left">
              <span className="text-[10px] bg-red-200 text-red-700 px-1.5 py-0.2 rounded-sm font-bold">!</span>
              <p className="text-[11px] text-red-600 font-semibold leading-relaxed my-auto">{error}</p>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary hover:bg-primary-light text-white font-bold text-xs py-3 rounded-xl disabled:opacity-50 cursor-pointer transition-colors shadow-xs ripple-container"
          >
            {loading ? '正在递交核实信息...' : '提交系统审核发布'}
          </button>
        </form>
      </div>
    </Layout>
  )
}
