import { useState, useEffect } from 'react'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
} from 'chart.js'
import { Pie, Bar } from 'react-chartjs-2'
import request from '../../utils/request'
import type { Statistics as StatsType } from '../../types'

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title)

const STATUS_LABELS: Record<string, string> = {
  pending: '待审核',
  active: '已上架',
  rejected: '已拒绝',
  offline: '已下架',
  sold: '已售出',
}

const STATUS_COLORS: Record<string, string> = {
  pending: '#f59e0b',
  active: '#10b981',
  rejected: '#ef4444',
  offline: '#94a3b8',
  sold: '#6366f1',
}

export default function Statistics() {
  const [stats, setStats] = useState<StatsType | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    request.get('/admin/statistics/').then((res: any) => {
      setStats(res.data)
    }).catch(() => {
      // ignore
    }).finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div className="text-center py-10 text-neutral-500 text-xs font-semibold">正在载入系统最新总控快照...</div>
  }

  if (!stats) {
    return <div className="text-center py-10 text-red-500 text-xs font-semibold">载入快照失败</div>
  }

  const statusEntries = Object.entries(stats.products_by_status || {})
  const totalProducts = statusEntries.reduce((sum, [, v]) => sum + v, 0)

  const pieData = {
    labels: statusEntries.map(([k]) => STATUS_LABELS[k] || k),
    datasets: [{
      data: statusEntries.map(([, v]) => v),
      backgroundColor: statusEntries.map(([k]) => STATUS_COLORS[k] || '#6366f1'),
      borderWidth: 2,
      borderColor: '#ffffff',
      hoverBorderWidth: 3,
      hoverOffset: 8,
    }],
  }

  const barData = {
    labels: statusEntries.map(([k]) => STATUS_LABELS[k] || k),
    datasets: [{
      label: '商品数量',
      data: statusEntries.map(([, v]) => v),
      backgroundColor: statusEntries.map(([k]) => STATUS_COLORS[k] || '#6366f1'),
      borderRadius: 6,
      borderSkipped: false,
      barThickness: 36,
      hoverBackgroundColor: statusEntries.map(([k]) => {
        const c = STATUS_COLORS[k] || '#6366f1'
        return c + 'dd'
      }),
    }],
  }

  return (
    <div className="select-none text-left space-y-6">
      {/* Cards stats list */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-neutral-100 shadow-sm hover:shadow-md transition-shadow">
          <span className="text-[10px] text-neutral-400 font-bold uppercase tracking-widest mb-1.5 block">平台注册用户</span>
          <span className="text-2xl font-black text-neutral-900 tracking-tight">{stats.total_users}</span>
          <span className="text-[10px] text-neutral-400 ml-1">人</span>
        </div>
        <div className="bg-white p-5 rounded-xl border border-neutral-100 shadow-sm hover:shadow-md transition-shadow">
          <span className="text-[10px] text-neutral-400 font-bold uppercase tracking-widest mb-1.5 block">全站发布宝贝</span>
          <span className="text-2xl font-black text-neutral-900 tracking-tight">{stats.total_products}</span>
          <span className="text-[10px] text-neutral-400 ml-1">件</span>
        </div>
        <div className="bg-white p-5 rounded-xl border border-neutral-100 shadow-sm hover:shadow-md transition-shadow">
          <span className="text-[10px] text-neutral-400 font-bold uppercase tracking-widest mb-1.5 block">今日交易新增</span>
          <span className="text-2xl font-black text-emerald-600 tracking-tight">+{stats.today_new_products}</span>
          <span className="text-[10px] text-neutral-400 ml-1">件</span>
        </div>
        <div className="bg-white p-5 rounded-xl border border-neutral-100 shadow-sm hover:shadow-md transition-shadow">
          <span className="text-[10px] text-neutral-400 font-bold uppercase tracking-widest mb-1.5 block">急需审核处理</span>
          <span className={`text-2xl font-black tracking-tight ${stats.pending_products > 0 ? 'text-amber-500 animate-pulse' : 'text-neutral-300'}`}>
            {stats.pending_products}
          </span>
          <span className="text-[10px] text-neutral-400 ml-1">件</span>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Doughnut Chart */}
        <div className="bg-white p-6 rounded-xl border border-neutral-100 shadow-sm hover:shadow-md transition-shadow">
          <h3 className="text-xs text-neutral-500 font-bold tracking-wider mb-6 uppercase">
            上架状态比率
            <span className="text-neutral-300 ml-2">状态占比</span>
          </h3>
          {statusEntries.length > 0 ? (
            <div className="flex items-center gap-6">
              <div className="w-[180px] h-[180px] flex-shrink-0">
                <Pie
                  data={pieData}
                  options={{
                    cutout: '55%',
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                      legend: { display: false },
                      tooltip: {
                        backgroundColor: 'rgba(0,0,0,0.8)',
                        titleFont: { size: 12, weight: 'bold' },
                        bodyFont: { size: 11 },
                        padding: 10,
                        cornerRadius: 8,
                        callbacks: {
                          label: (ctx) => {
                            const pct = totalProducts > 0 ? ((ctx.parsed / totalProducts) * 100).toFixed(1) : '0'
                            return ` ${ctx.label}: ${ctx.parsed} 件 (${pct}%)`
                          },
                        },
                      },
                    },
                    animation: { animateRotate: true, duration: 800 },
                  }}
                />
              </div>
              {/* Legend */}
              <div className="flex flex-col gap-2.5 text-xs">
                {statusEntries.map(([k, v]) => {
                  const pct = totalProducts > 0 ? ((v / totalProducts) * 100).toFixed(1) : '0'
                  return (
                    <div key={k} className="flex items-center gap-2">
                      <span
                        className="w-2.5 h-2.5 rounded-full flex-shrink-0"
                        style={{ backgroundColor: STATUS_COLORS[k] || '#6366f1' }}
                      />
                      <span className="text-neutral-600 whitespace-nowrap">{STATUS_LABELS[k] || k}</span>
                      <span className="text-neutral-900 font-bold ml-auto tabular-nums">{pct}%</span>
                    </div>
                  )
                })}
              </div>
            </div>
          ) : (
            <p className="text-center py-12 text-neutral-400 text-xs">暂无状态统计数据</p>
          )}
        </div>

        {/* Bar Chart */}
        <div className="bg-white p-6 rounded-xl border border-neutral-100 shadow-sm hover:shadow-md transition-shadow">
          <h3 className="text-xs text-neutral-500 font-bold tracking-wider mb-6 uppercase">
            数量柱状分布
            <span className="text-neutral-300 ml-2">柱状图</span>
          </h3>
          {statusEntries.length > 0 ? (
            <div className="w-full h-[220px]">
              <Bar
                data={barData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: { display: false },
                    tooltip: {
                      backgroundColor: 'rgba(0,0,0,0.8)',
                      titleFont: { size: 12, weight: 'bold' },
                      bodyFont: { size: 11 },
                      padding: 10,
                      cornerRadius: 8,
                      callbacks: {
                        label: (ctx) => ` ${ctx.parsed.y} 件`,
                      },
                    },
                  },
                  scales: {
                    y: {
                      beginAtZero: true,
                      ticks: {
                        stepSize: 1,
                        font: { size: 11 },
                        color: '#a3a3a3',
                      },
                      grid: { color: 'rgba(0,0,0,0.04)' },
                      border: { display: false },
                    },
                    x: {
                      ticks: {
                        font: { size: 11, weight: 'bold' },
                        color: '#525252',
                      },
                      grid: { display: false },
                      border: { display: false },
                    },
                  },
                  animation: { duration: 800, easing: 'easeOutQuart' },
                }}
              />
            </div>
          ) : (
            <p className="text-center py-12 text-neutral-400 text-xs">暂无分布统计数据</p>
          )}
        </div>
      </div>
    </div>
  )
}
