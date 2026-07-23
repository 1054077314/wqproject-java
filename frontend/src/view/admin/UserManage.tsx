import { useState, useEffect } from 'react'
import request from '../../utils/request'
import { useToast } from '../../context/ToastContext'
import type { AdminUser, PaginatedRes } from '../../types'

export default function UserManage() {
  const { toast, confirm } = useToast()
  const [users, setUsers] = useState<AdminUser[]>([])
  const [loading, setLoading] = useState(true)

  const loadUsers = async () => {
    setLoading(true)
    try {
      const res: PaginatedRes<AdminUser> = await request.get('/admin/users/')
      setUsers(res.results)
    } catch {
      // ignore
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  async function toggleUser(id: number, currentActive: boolean) {
    confirm({
      title: '账户状态管控',
      message: `您确定要 ${currentActive ? '封禁禁用' : '解封启用'} 该校园二手交易账户吗？被封禁的账户将无法参与任何交易及提问。`,
      confirmText: currentActive ? '确认封禁' : '确认解禁',
      onConfirm: async () => {
        try {
          await request.put(`/admin/users/${id}/`, { is_active: !currentActive })
          setUsers(prev => prev.map(u => u.id === id ? { ...u, is_active: !currentActive } : u))
          toast(currentActive ? '该账户已被禁用封锁。' : '该账户已恢复正常使用！', 'success')
        } catch (e: any) {
          toast(e?.message || '操作失败', 'error')
        }
      }
    })
  }

  if (loading) {
    return <div className="text-center py-10 text-neutral-500 text-xs font-semibold">加载用户角色列表中...</div>
  }

  return (
    <div className="bg-white rounded-2xl border border-primary-faint shadow-xs overflow-hidden select-none">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-neutral-500 font-medium">
          <thead className="bg-page-bg border-b border-primary-faint text-neutral-500 font-bold uppercase tracking-wider whitespace-nowrap">
            <tr>
              <th className="px-5 py-3">用户编号</th>
              <th className="px-5 py-3">账户登录名</th>
              <th className="px-5 py-3">注册创建时间</th>
              <th className="px-5 py-3">权控状态</th>
              <th className="px-5 py-3 text-right">安全管控操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-primary-subtle text-neutral-800 font-semibold whitespace-nowrap">
            {users.map(u => (
              <tr key={u.id} className="hover:bg-primary-subtle transition-colors">
                <td className="px-5 py-3.5 font-mono text-neutral-500">#{u.id}</td>
                <td className="px-5 py-3.5 flex items-center gap-2">
                  <span className="font-extrabold text-neutral-900">@{u.username}</span>
                  {u.is_staff && (
                    <span className="text-[9px] bg-accent border border-accent-dark text-neutral-800 px-2 py-0.5 rounded-md font-bold uppercase tracking-widest scale-95 shrink-0 select-none">
                      STAFF
                    </span>
                  )}
                </td>
                <td className="px-5 py-3.5 text-neutral-500 text-[11px] font-mono">{new Date(u.created_at).toLocaleString()}</td>
                <td className="px-5 py-3.5">
                  <span className={`inline-block px-2.5 py-0.5 text-[10px] font-black border rounded-md uppercase tracking-wider ${
                    u.is_active
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-250/50'
                      : 'bg-red-50 text-red-700 border-red-200'
                  }`}>
                    {u.is_active ? '正常流通' : '已封禁'}
                  </span>
                </td>
                <td className="px-5 py-3.5 text-right">
                  <button
                    onClick={() => toggleUser(u.id, u.is_active)}
                    disabled={u.is_staff}
                    className={`text-[10px] px-3.5 py-1.5 rounded-xl border font-black uppercase tracking-wider disabled:opacity-[0.25] disabled:cursor-not-allowed cursor-pointer transition-all ripple-container ${
                      u.is_active
                        ? 'bg-white text-red-600 border-primary-faint hover:border-red-600 hover:bg-red-50'
                        : 'bg-primary text-white border-primary hover:bg-primary-light'
                    }`}
                  >
                    {u.is_active ? '封禁禁用' : '解除封锁'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {users.length === 0 && (
        <p className="text-center py-10 text-neutral-500 text-xs font-semibold">平台目前还没有注册交易用户</p>
      )}
    </div>
  )
}
