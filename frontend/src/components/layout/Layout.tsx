import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  Home,
  Search
} from 'lucide-react'
import type { ReactNode } from 'react'

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [navSearch, setNavSearch] = useState('')

  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/'
    }
    return location.pathname.startsWith(path)
  }

  const linkClass = (path: string) => {
    return `inline-flex items-center gap-1.5 text-xs font-bold transition-all pb-1 border-b cursor-pointer ${
      isActive(path)
        ? 'text-neutral-900 border-primary'
        : 'text-neutral-500 border-transparent hover:text-neutral-900'
    }`
  }

  function handleNavSearch(e: FormEvent) {
    e.preventDefault()
    const q = navSearch.trim()
    if (q) {
      navigate(`/products?search=${encodeURIComponent(q)}`)
    } else {
      navigate('/products')
    }
  }

  return (
    <div className="min-h-screen bg-white text-neutral-800 flex flex-col justify-between font-sans selection:bg-accent selection:text-neutral-900">
      <div>
        {/* Navigation Bar */}
        <header className="bg-white border-b border-primary-faint sticky top-0 z-40">
          <div className="max-w-5xl mx-auto px-6 py-4 flex justify-between items-center gap-4">
            {/* Branding */}
            <Link to="/" className="inline-flex items-center gap-2 group shrink-0">
              <span className="text-sm font-black text-neutral-900 tracking-wider font-mono">
                校园闲置交易
              </span>
            </Link>

            {/* Global search entry */}
            <form onSubmit={handleNavSearch} className="hidden sm:block relative flex-1 max-w-[220px]">
              <input
                type="search"
                value={navSearch}
                onChange={e => setNavSearch(e.target.value)}
                placeholder="搜闲置…"
                className="w-full pl-8 pr-3 py-1.5 border border-primary-faint rounded-lg text-xs font-semibold text-neutral-800 placeholder:text-neutral-400 focus:outline-hidden focus:border-primary"
                aria-label="搜索商品"
              />
              <Search className="absolute left-2.5 top-2 h-3.5 w-3.5 text-neutral-400 pointer-events-none" />
            </form>

            {/* Nav links */}
            <nav className="flex items-center gap-5 md:gap-7 shrink-0">
              <Link to="/" className={linkClass('/')}>
                <span className="hidden sm:inline">首页</span>
                <span className="sm:hidden"><Home className="h-4 w-4" /></span>
              </Link>
              <Link to="/products" className={linkClass('/products')}>
                <span>宝贝列表</span>
              </Link>

              {user ? (
                <>
                  <Link to="/publish" className={linkClass('/publish')}>
                    <span>发布商品</span>
                  </Link>
                  <Link to="/profile" className={linkClass('/profile')}>
                    <span>个人中心</span>
                  </Link>
                  {user.is_staff && (
                    <Link to="/admin" className={linkClass('/admin')}>
                      <span className="text-neutral-900 font-bold">审核后台</span>
                    </Link>
                  )}

                  <div className="h-3 w-[1px] bg-primary-faint" />

                  {/* Logged in User */}
                  <div className="flex items-center gap-2.5">
                    <span className="text-[11px] text-neutral-500 font-bold hidden md:inline-block">
                      @{user.username}
                    </span>
                    <button
                      onClick={logout}
                      className="inline-flex items-center gap-1 text-xs font-bold text-neutral-500 hover:text-neutral-900 transition-colors cursor-pointer"
                      title="退出登录"
                    >
                      <span>退出</span>
                    </button>
                  </div>
                </>
              ) : (
                <Link
                  to="/login"
                  className="font-bold text-xs bg-primary text-white px-3.5 py-1.5 rounded-lg hover:bg-primary-light transition-all shadow-xs"
                >
                  <span>登录</span>
                </Link>
              )}
            </nav>
          </div>
        </header>

        {/* Content Area */}
        <main className="max-w-5xl mx-auto px-6 py-10 page-fade-in">
          {children}
        </main>
      </div>

      {/* Styled Footer */}
      <footer className="bg-white border-t border-primary-faint mt-16 text-neutral-500 py-8 text-xs font-medium">
        <div className="max-w-5xl mx-auto px-6 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="text-center md:text-left">
            <span className="text-xs font-bold text-neutral-900 tracking-wider">校园闲置交易</span>
            <p className="text-[11px] text-neutral-500 mt-1 leading-relaxed">
              倡导极简与绿色环保的校园二手商品直交流转枢纽。
            </p>
          </div>
          <div className="flex gap-x-5 justify-center text-[11px] font-bold text-neutral-500">
            <Link to="/" className="hover:text-neutral-900 transition-colors">首页</Link>
            <Link to="/products" className="hover:text-neutral-900 transition-colors">全部大厅</Link>
            <Link to="/publish" className="hover:text-neutral-900 transition-colors">上架闲置</Link>
            <Link to="/profile" className="hover:text-neutral-900 transition-colors">预约订单</Link>
          </div>
          <p className="text-[10px] text-neutral-500">
            &copy; 2026 校园闲置交易平台
          </p>
        </div>
      </footer>
    </div>
  )
}
