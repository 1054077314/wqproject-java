import { useState, useEffect, useCallback } from 'react'
import request from '../../utils/request'
import type { AuditLogItem, PaginatedRes } from '../../types'

const ACTION_LABEL: Record<string, string> = {
  'product.approve': '审核通过',
  'product.reject': '审核驳回',
  'product.offline': '商品下架',
  'appointment.confirm': '确认成交',
  'appointment.reject': '拒绝预约',
  'appointment.cancel': '取消预约',
  'user.enable': '启用用户',
  'user.disable': '禁用用户',
}

const ACTION_OPTIONS = [
  { value: '', label: '全部动作' },
  ...Object.entries(ACTION_LABEL).map(([value, label]) => ({ value, label })),
]

const PAGE_SIZE = 10

export default function AuditLogManage() {
  const [logs, setLogs] = useState<AuditLogItem[]>([])
  const [loading, setLoading] = useState(true)
  const [action, setAction] = useState('')
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)

  const loadLogs = useCallback(async () => {
    setLoading(true)
    try {
      const params: Record<string, string> = {
        page: String(page),
        page_size: String(PAGE_SIZE),
      }
      if (action) params.action = action
      const res: PaginatedRes<AuditLogItem> = await request.get('/admin/audit-logs/', { params })
      setLogs(res.data?.results ?? [])
      setTotal(typeof res.data?.count === 'number' ? res.data.count : 0)
    } catch {
      setLogs([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }, [page, action])

  useEffect(() => {
    loadLogs()
  }, [loadLogs])

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))

  function handleActionChange(next: string) {
    setAction(next)
    setPage(1)
  }

  return (
    <div className="bg-white rounded-2xl border border-primary-faint shadow-xs overflow-hidden select-none">
      <div className="px-5 py-4 border-b border-primary-faint flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h2 className="text-sm font-black text-neutral-900 tracking-tight">操作审计留痕</h2>
          <p className="text-[11px] text-neutral-500 font-semibold mt-1">
            记录审核、成交、下架、启停用户等敏感操作，便于追溯责任人。
          </p>
        </div>
        <label className="flex items-center gap-2 text-[11px] font-bold text-neutral-500">
          <span>动作筛选</span>
          <select
            value={action}
            onChange={e => handleActionChange(e.target.value)}
            className="border border-primary-faint rounded-lg px-2.5 py-1.5 text-xs font-semibold text-neutral-800 bg-white focus:outline-hidden focus:border-primary"
          >
            {ACTION_OPTIONS.map(opt => (
              <option key={opt.value || 'all'} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </label>
      </div>

      {loading ? (
        <div className="text-center py-10 text-neutral-500 text-xs font-semibold">加载审计记录中...</div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-neutral-500 font-medium">
              <thead className="bg-page-bg border-b border-primary-faint text-neutral-500 font-bold uppercase tracking-wider whitespace-nowrap">
                <tr>
                  <th className="px-5 py-3">时间</th>
                  <th className="px-5 py-3">操作人</th>
                  <th className="px-5 py-3">动作</th>
                  <th className="px-5 py-3">资源</th>
                  <th className="px-5 py-3">详情</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary-subtle text-neutral-800 font-semibold whitespace-nowrap">
                {logs.map(log => (
                  <tr key={log.id} className="hover:bg-primary-subtle transition-colors">
                    <td className="px-5 py-3.5 text-neutral-500 text-[11px] font-mono">
                      {new Date(log.created_at).toLocaleString()}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className="font-extrabold text-neutral-900">
                        @{log.actor_username || 'system'}
                      </span>
                      {log.actor_id != null && (
                        <span className="ml-2 font-mono text-neutral-500 text-[10px]">#{log.actor_id}</span>
                      )}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className="inline-block px-2.5 py-0.5 text-[10px] font-black border rounded-md uppercase tracking-wider bg-primary-subtle text-neutral-700 border-primary-faint">
                        {ACTION_LABEL[log.action] || log.action}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 font-mono text-[11px] text-neutral-600">
                      {log.resource_type}
                      {log.resource_id != null ? ` #${log.resource_id}` : ''}
                    </td>
                    <td className="px-5 py-3.5 text-[11px] text-neutral-500 max-w-xs truncate" title={log.detail || ''}>
                      {log.detail || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {logs.length === 0 && (
            <p className="text-center py-10 text-neutral-500 text-xs font-semibold">
              暂无审计记录。完成审核、成交或启停用户后会出现在这里。
            </p>
          )}
          {total > 0 && (
            <div className="px-5 py-3 border-t border-primary-faint flex items-center justify-between gap-3">
              <span className="text-[11px] text-neutral-500 font-semibold">
                共 {total} 条 · 第 {page}/{totalPages} 页
              </span>
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={page <= 1}
                  onClick={() => setPage(p => Math.max(1, p - 1))}
                  className="px-3 py-1.5 text-[11px] font-bold rounded-lg border border-primary-faint disabled:opacity-40 hover:border-primary cursor-pointer"
                >
                  上一页
                </button>
                <button
                  type="button"
                  disabled={page >= totalPages}
                  onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                  className="px-3 py-1.5 text-[11px] font-bold rounded-lg border border-primary-faint disabled:opacity-40 hover:border-primary cursor-pointer"
                >
                  下一页
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
