import { useState, useEffect } from 'react'
import request from '../../utils/request'
import { useToast } from '../../context/ToastContext'
import type { Category, ApiRes } from '../../types'

export default function CategoryManage() {
  const { toast, confirm } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [newName, setNewName] = useState('')
  const [editId, setEditId] = useState<number | null>(null)
  const [editName, setEditName] = useState('')
  const [error, setError] = useState('')

  const loadCategories = async () => {
    setLoading(true)
    try {
      const res: ApiRes<Category[]> = await request.get('/categories/')
      const data = Array.isArray(res) ? res : res.data
      setCategories(Array.isArray(data) ? data : [])
    } catch {
      // ignore
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCategories()
  }, [])

  async function handleCreate() {
    if (!newName.trim()) return
    setError('')
    try {
      await request.post('/admin/categories/', { name: newName.trim() })
      setNewName('')
      loadCategories()
      toast('成功新建品类分类！', 'success')
    } catch (e: any) {
      setError(e?.message || '新建品类分类失败')
    }
  }

  async function handleUpdate() {
    if (!editId || !editName.trim()) return
    setError('')
    try {
      await request.put(`/admin/categories/${editId}/`, { name: editName.trim() })
      setEditId(null)
      setEditName('')
      loadCategories()
      toast('品类分类更新成功', 'success')
    } catch (e: any) {
      setError(e?.message || '更新品类分类失败')
    }
  }

  async function handleDelete(id: number, name: string) {
    confirm({
      title: '删除品类分类',
      message: `您确定要永久删除 "${name}" 这个分类吗？属于该板块的二手商品可能会受到影响！`,
      confirmText: '确认删除',
      onConfirm: async () => {
        try {
          await request.delete(`/admin/categories/${id}/`)
          toast('成功删除分类！', 'success')
          loadCategories()
        } catch (e: any) {
          toast(e?.message || '删除分类失败', 'error')
        }
      }
    })
  }

  if (loading) {
    return <div className="text-center py-10 text-neutral-500 text-xs font-semibold">加载分类配置中...</div>
  }

  return (
    <div className="space-y-6 text-left select-none">
      {/* Create New Column */}
      <div className="bg-white rounded-2xl border border-primary-faint p-5">
        <h3 className="text-[10px] text-neutral-500 font-bold tracking-widest mb-3.5">添加新品类交易板块</h3>
        <div className="flex gap-2.5">
          <input
            type="text"
            value={newName}
            onChange={e => setNewName(e.target.value)}
            placeholder="如美妆护肤、户外骑行、生活百货..."
            className="flex-1 px-3 py-2.5 bg-primary-subtle border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary focus:bg-white text-xs font-semibold text-neutral-800 placeholder:text-neutral-500"
          />
          <button
            onClick={handleCreate}
            disabled={!newName.trim()}
            className="bg-primary hover:bg-primary-light text-white font-bold text-xs px-4.5 py-2.5 rounded-xl disabled:opacity-50 transition-all cursor-pointer whitespace-nowrap ripple-container"
          >
            新建立即上架
          </button>
        </div>
        {error && <p className="text-red-500 text-[10px] font-bold mt-2 select-none">{error}</p>}
      </div>

      {/* Grid configuration list */}
      <div className="bg-white rounded-2xl border border-primary-faint shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-neutral-500">
            <thead className="bg-page-bg border-b border-primary-faint text-neutral-500 font-bold uppercase tracking-wider whitespace-nowrap">
              <tr>
                <th className="px-5 py-3">分类编号</th>
                <th className="px-5 py-3">分类名称</th>
                <th className="px-5 py-3 text-right">管理操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary-subtle text-neutral-800 font-semibold whitespace-nowrap">
              {categories.map(c => (
                <tr key={c.id} className="hover:bg-primary-subtle transition-colors">
                  <td className="px-5 py-3.5 font-mono text-neutral-500">#{c.id}</td>
                  <td className="px-5 py-3.5">
                    {editId === c.id ? (
                      <input
                        type="text"
                        value={editName}
                        onChange={e => setEditName(e.target.value)}
                        className="px-2.5 py-1 bg-white border border-primary rounded-lg text-xs font-bold focus:outline-hidden text-neutral-800"
                        autoFocus
                        onKeyDown={e => {
                          if (e.key === 'Enter') handleUpdate()
                          else if (e.key === 'Escape') setEditId(null)
                        }}
                      />
                    ) : (
                      <span className="font-extrabold text-neutral-900">{c.name}</span>
                    )}
                  </td>
                  <td className="px-5 py-3.5 text-right space-x-3.5">
                    {editId === c.id ? (
                      <>
                        <button onClick={handleUpdate} className="text-xs text-emerald-600 hover:underline cursor-pointer">保存</button>
                        <button onClick={() => setEditId(null)} className="text-xs text-neutral-500 hover:underline cursor-pointer">取消</button>
                      </>
                    ) : (
                      <>
                        <button
                          onClick={() => { setEditId(c.id); setEditName(c.name) }}
                          className="text-[11px] text-neutral-500 hover:text-neutral-900 font-extrabold cursor-pointer"
                        >
                          重命名
                        </button>
                        <button
                          onClick={() => handleDelete(c.id, c.name)}
                          className="text-[11px] text-red-500 hover:text-red-700 font-extrabold cursor-pointer"
                        >
                          删除
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {categories.length === 0 && (
          <p className="text-center py-10 text-neutral-500 text-xs font-semibold">配置中还没有任何品类板块呢</p>
        )}
      </div>
    </div>
  )
}
