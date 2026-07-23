import { useState, useEffect } from 'react'
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

export default function AuditLogManage() {
  const [logs, setLogs] = useState<AuditLogItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      try {
        const res: PaginatedRes<AuditLogItem> = await request.get('/admin/audit-logs/')
        if (!cancelled) {
          setLogs(res.data?.results ?? [])
        }
      } catch {
        if (!cancelled) {
          setLogs([])
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    })()
    return () => { cancelled = true }
  }, [])

  if (loading) {
    return <div className="text-center py-10 text-neutral-500 text-xs font-semibold">加载审计记录中...</div>
  }

  return (
    <div className="bg-white rounded-2xl border border-primary-faint shadow-xs overflow-hidden select-none">
      <div className="px-5 py-4 border-b border-primary-faint">
        <h2 className="text-sm font-black text-neutral-900 tracking-tight">操作审计留痕</h2>
        <p className="text-[11px] text-neutral-500 font-semibold mt-1">
          记录审核、成交、下架、启停用户等敏感操作，便于追溯责任人。
        </p>
      </div>
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
    </div>
  )
}
