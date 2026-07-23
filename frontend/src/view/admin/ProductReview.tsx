import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Check, X, ShieldAlert } from 'lucide-react'
import request from '../../utils/request'
import { useToast } from '../../context/ToastContext'
import type { ProductListItem, PaginatedRes } from '../../types'

export default function ProductReview() {
  const { toast } = useToast()
  const [products, setProducts] = useState<ProductListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [rejectId, setRejectId] = useState<number | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const loadProducts = async () => {
    setLoading(true)
    try {
      const res: PaginatedRes<ProductListItem> = await request.get('/admin/pending-products/')
      setProducts(res.results)
    } catch {
      // ignore
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProducts()
  }, [])

  async function handleApprove(id: number) {
    try {
      await request.post(`/admin/products/${id}/review/`, { action: 'approve' })
      setProducts(prev => prev.filter(p => p.id !== id))
      toast('宝贝审核批准！商品已正式上架。', 'success')
    } catch (e: any) {
      toast(e?.message || '审核批准操作失败', 'error')
    }
  }

  async function handleReject() {
    if (!rejectId || !rejectReason.trim()) return
    try {
      await request.post(`/admin/products/${rejectId}/review/`, {
        action: 'reject',
        reject_reason: rejectReason
      })
      setProducts(prev => prev.filter(p => p.id !== rejectId))
      setRejectId(null)
      setRejectReason('')
      toast('宝贝驳回成功，已发送下架通知。', 'info')
    } catch (e: any) {
      toast(e?.message || '审核拒绝操作失败', 'error')
    }
  }

  if (loading) {
    return <div className="text-center py-10 text-neutral-500 text-xs font-semibold">加载待审核商品队列中...</div>
  }

  return (
    <div className="select-none text-left">
      {products.length === 0 ? (
        <div className="bg-white rounded-xl border border-primary-faint p-12 text-center">
          <p className="text-neutral-500 text-xs font-bold tracking-wider">暂无待审核好物</p>
          <p className="text-xs text-neutral-500 mt-1">棒极了！目前校园流转商品队列均已审核完毕。</p>
        </div>
      ) : (
        <div className="space-y-4">
          {products.map(p => (
            <div key={p.id} className="bg-white border border-primary-faint p-5 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-4 card-float">
              <div className="flex items-center gap-4 min-w-0">
                <div className="w-16 h-16 bg-primary-subtle rounded-xl overflow-hidden shrink-0 flex items-center justify-center border border-primary-faint">
                  {p.first_image ? (
                    <img src={p.first_image} alt="" className="w-full h-full object-cover pointer-events-none" />
                  ) : (
                    <span className="text-neutral-400 text-[10px]">暂无图片</span>
                  )}
                </div>
                <div className="min-w-0">
                  <Link to={`/products/${p.id}`} className="font-extrabold text-xs text-neutral-900 hover:text-neutral-800 hover:underline truncate block">
                    {p.title}
                  </Link>
                  <div className="flex gap-2.5 items-center mt-2 text-[10px] text-neutral-500 font-bold uppercase tracking-wider">
                    <span>品类: {p.category_name}</span>
                    <span>•</span>
                    <span>售价: <span className="text-neutral-900 font-black">¥{p.price}</span></span>
                  </div>
                </div>
              </div>
              <div className="flex gap-2 shrink-0 self-end sm:self-center">
                <button
                  onClick={() => handleApprove(p.id)}
                  className="inline-flex items-center gap-1 bg-primary hover:bg-primary-light text-white font-bold text-[11px] px-4 py-2 rounded-xl transition-colors cursor-pointer ripple-container"
                >
                  <Check className="h-3.5 w-3.5" />
                  <span>批准上架</span>
                </button>
                <button
                  onClick={() => { setRejectId(p.id); setRejectReason('') }}
                  className="inline-flex items-center gap-1 bg-white border border-primary-faint hover:border-red-600 hover:text-red-600 text-neutral-500 font-bold text-[11px] px-4 py-2 rounded-xl transition-colors cursor-pointer ripple-container"
                >
                  <X className="h-3.5 w-3.5" />
                  <span>拒绝驳回</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Reject Modal */}
      {rejectId !== null && (
        <div className="fixed inset-0 bg-primary-dark/20 backdrop-blur-xs flex items-center justify-center z-50 p-6">
          <div className="bg-white p-6 rounded-2xl border border-primary-faint max-w-sm w-full shadow-lg">

            <div className="flex items-center gap-2 mb-3 text-red-600">
              <ShieldAlert className="h-5 w-5 shrink-0" />
              <h3 className="text-xs font-black tracking-widest">填写驳回原因</h3>
            </div>

            <textarea
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
              maxLength={200}
              rows={3}
              placeholder="请输入真实的驳回依据，平台将以此给对方发送私信以便其修订（如：配图模糊、信息虚假等）..."
              className="w-full px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 leading-normal mb-4"
            />

            <div className="flex justify-end gap-2 text-xs font-bold">
              <button
                onClick={() => setRejectId(null)}
                className="px-4 py-2 border border-primary-faint text-neutral-500 rounded-xl hover:text-neutral-900 transition-colors cursor-pointer"
              >
                取消
              </button>
              <button
                onClick={handleReject}
                disabled={!rejectReason.trim()}
                className="px-4 py-2 bg-primary hover:bg-primary-light text-white rounded-xl transition-all disabled:opacity-50 cursor-pointer ripple-container"
              >
                确认拒绝
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
