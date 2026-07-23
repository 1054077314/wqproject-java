import { useState } from 'react'
import {
  BarChart4,
  Users2,
  CheckSquare,
  SlidersHorizontal,
  type LucideIcon
} from 'lucide-react'
import Layout from '../../components/layout/Layout'
import Statistics from './Statistics'
import UserManage from './UserManage'
import ProductReview from './ProductReview'
import CategoryManage from './CategoryManage'

type AdminTab = 'stats' | 'users' | 'review' | 'categories'

export default function AdminLayout() {
  const [tab, setTab] = useState<AdminTab>('stats')

  const tabs: { key: AdminTab; label: string; icon: LucideIcon }[] = [
    { key: 'stats', label: '状态数据看板', icon: BarChart4 },
    { key: 'users', label: '平台用户管理', icon: Users2 },
    { key: 'review', label: '商品审核管理', icon: CheckSquare },
    { key: 'categories', label: '分类板块配置', icon: SlidersHorizontal },
  ]

  return (
    <Layout>
      <div className="mb-6 select-none text-left">
        <h1 className="text-xl font-bold tracking-tight text-neutral-900">平台安全总控后台</h1>
        <p className="text-xs text-neutral-500 mt-1 font-semibold leading-relaxed">
          管理员专属平台安全配置决策中枢。包含商品过滤排队审查、校园分类定制、以及交易端用户封锁激活等关键决策工具。
        </p>
      </div>

      {/* Tabs list with minimalist theme buttons */}
      <div className="flex flex-wrap gap-2 border-b border-primary-faint pb-4 mb-8 select-none">
        {tabs.map(t => {
          const Icon = t.icon
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ripple-container ${
                tab === t.key
                  ? 'bg-primary text-white'
                  : 'bg-white border border-primary-faint text-neutral-500 hover:text-neutral-900 hover:border-primary-muted'
              }`}
            >
              <Icon className="h-3.5 w-3.5" />
              <span>{t.label}</span>
            </button>
          )
        })}
      </div>

      <div className="min-h-96">
        {tab === 'stats' && <Statistics />}
        {tab === 'users' && <UserManage />}
        {tab === 'review' && <ProductReview />}
        {tab === 'categories' && <CategoryManage />}
      </div>
    </Layout>
  )
}
