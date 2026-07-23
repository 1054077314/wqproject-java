import { useState, useEffect, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Search,
  Laptop,
  BookOpen,
  Dumbbell,
  ShoppingBag,
  ArrowRight,
  ShieldCheck,
  Leaf,
  Users
} from 'lucide-react'
import request from '../../utils/request'
import Layout from '../../components/layout/Layout'
import type { ProductListItem, PaginatedRes } from '../../types'

export default function Home() {
  const navigate = useNavigate()
  const [searchWord, setSearchWord] = useState('')
  const [latestProducts, setLatestProducts] = useState<ProductListItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function fetchLatest() {
      try {
        const res = await request.get('/products/') as PaginatedRes<ProductListItem>
        if (res?.data && Array.isArray(res.data.results)) {
          setLatestProducts(res.data.results.slice(0, 4))
        }
      } catch {
        // ignore
      } finally {
        setLoading(false)
      }
    }
    fetchLatest()
  }, [])

  function handleSearchSubmit(e: FormEvent) {
    e.preventDefault()
    if (searchWord.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchWord.trim())}`)
    } else {
      navigate('/products')
    }
  }

  return (
    <Layout>
      {/* 1. Quiet, Typographic Main Header */}
      <div className="py-12 md:py-16 text-left border-b border-primary-faint mb-12 select-none">
        <div className="inline-flex items-center gap-1.5 border border-primary-faint text-neutral-500 text-[10px] tracking-wider uppercase font-mono px-2.5 py-0.5 rounded-sm mb-5">
          <span>校内免中介 · 安全实名循环</span>
        </div>

        <h1 className="text-3xl md:text-5xl font-black tracking-tight text-neutral-900 leading-tight">
          极简主义的校园二手平台。
        </h1>
        <p className="mt-4 text-xs md:text-sm text-neutral-500 max-w-lg leading-relaxed font-semibold">
          专为在校本校学子打造的精简闲置流转大厅。让每本书教材、数码电子、日常生活器具安全回归到需要它的人手中，零平台佣金，线下实名面交。
        </p>

        {/* Minimal Search Field */}
        <form onSubmit={handleSearchSubmit} className="mt-8 max-w-md flex gap-2">
          <div className="relative flex-1">
            <input
              type="text"
              value={searchWord}
              onChange={e => setSearchWord(e.target.value)}
              placeholder="搜索需要的课本、平板、自行车以及各种闲置..."
              className="w-full text-xs text-neutral-800 placeholder:text-neutral-500 pl-9 pr-4 py-3 bg-white border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary font-semibold shadow-xs"
            />
            <Search className="absolute left-3 top-3.5 h-4 w-4 text-neutral-500" />
          </div>
          <button
            type="submit"
            className="bg-primary hover:bg-primary-light transition-colors text-white text-xs font-bold px-5 py-3 rounded-xl cursor-pointer ripple-container"
          >
            搜索
          </button>
        </form>
      </div>

      {/* 2. Pure Border Category Pathway Grid */}
      <div className="mb-14">
        <h2 className="text-[10px] text-neutral-500 font-bold uppercase tracking-widest mb-5">
          品类直达
        </h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Link
            to="/products?category_id=1"
            className="group bg-white p-5 rounded-xl border border-primary-faint hover:border-primary transition-all text-left flex items-center gap-4 card-float ripple-container"
          >
            <div className="w-9 h-9 rounded-lg bg-primary-subtle flex items-center justify-center text-neutral-500 shrink-0 group-hover:bg-primary group-hover:text-white transition-all">
              <Laptop className="h-4.5 w-4.5" />
            </div>
            <div>
              <h3 className="font-extrabold text-xs text-neutral-900">电子数码</h3>
              <p className="text-[10px] text-neutral-500 mt-0.5">电脑、平板及硬件配件</p>
            </div>
          </Link>

          <Link
            to="/products?category_id=2"
            className="group bg-white p-5 rounded-xl border border-primary-faint hover:border-primary transition-all text-left flex items-center gap-4 card-float ripple-container"
          >
            <div className="w-9 h-9 rounded-lg bg-primary-subtle flex items-center justify-center text-neutral-500 shrink-0 group-hover:bg-primary group-hover:text-white transition-all">
              <BookOpen className="h-4.5 w-4.5" />
            </div>
            <div>
              <h3 className="font-extrabold text-xs text-neutral-900">图书教材</h3>
              <p className="text-[10px] text-neutral-500 mt-0.5">专业课本以及各类书籍</p>
            </div>
          </Link>

          <Link
            to="/products?category_id=3"
            className="group bg-white p-5 rounded-xl border border-primary-faint hover:border-primary transition-all text-left flex items-center gap-4 card-float ripple-container"
          >
            <div className="w-9 h-9 rounded-lg bg-primary-subtle flex items-center justify-center text-neutral-500 shrink-0 group-hover:bg-primary group-hover:text-white transition-all">
              <Dumbbell className="h-4.5 w-4.5" />
            </div>
            <div>
              <h3 className="font-extrabold text-xs text-neutral-900">运动器材</h3>
              <p className="text-[10px] text-neutral-500 mt-0.5">滑板、球拍或室内运动设备</p>
            </div>
          </Link>

          <Link
            to="/products?category_id=4"
            className="group bg-white p-5 rounded-xl border border-primary-faint hover:border-primary transition-all text-left flex items-center gap-4 card-float ripple-container"
          >
            <div className="w-9 h-9 rounded-lg bg-primary-subtle flex items-center justify-center text-neutral-500 shrink-0 group-hover:bg-primary group-hover:text-white transition-all">
              <ShoppingBag className="h-4.5 w-4.5" />
            </div>
            <div>
              <h3 className="font-extrabold text-xs text-neutral-900">生活用品</h3>
              <p className="text-[10px] text-neutral-500 mt-0.5">宿舍收纳、台灯或小物件</p>
            </div>
          </Link>
        </div>
      </div>

      {/* 3. Products Stream Grid Section */}
      <div className="mb-14">
        <div className="flex justify-between items-end mb-6">
          <div>
            <h2 className="text-[10px] text-neutral-500 font-bold uppercase tracking-widest">
              精选校园好物
            </h2>
            <p className="text-xs text-neutral-500 font-semibold mt-1">最近发布的本校活跃审核闲置，支持校内当面验货</p>
          </div>
          <Link
            to="/products"
            className="inline-flex items-center gap-1 text-xs text-neutral-900 font-bold hover:underline"
          >
            <span>全部商品列表</span>
            <ArrowRight className="h-3 w-3" />
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-5 animate-pulse">
            {[1, 2, 3, 4].map(n => (
              <div key={n} className="bg-primary-subtle rounded-xl h-64" />
            ))}
          </div>
        ) : latestProducts.length === 0 ? (
          <div className="bg-white rounded-xl border border-primary-faint p-12 text-center text-neutral-500 text-xs font-semibold">
            暂无活跃上架闲置。
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-5">
            {latestProducts.map(p => (
              <Link
                key={p.id}
                to={`/products/${p.id}`}
                className="group bg-white rounded-xl border border-primary-faint hover:border-primary transition-all overflow-hidden flex flex-col card-float"
              >
                <div className="aspect-square bg-primary-subtle flex items-center justify-center overflow-hidden relative">
                  {p.first_image ? (
                    <img
                      src={p.first_image}
                      alt={p.title}
                      className="w-full h-full object-cover group-hover:scale-102 transition-transform duration-300"
                    />
                  ) : (
                    <span className="text-neutral-400 text-xs font-medium">无图</span>
                  )}
                  <div className="absolute top-2 left-2 bg-accent text-neutral-800 text-[9px] px-2 py-0.5 rounded-sm font-bold tracking-wider uppercase">
                    ¥{p.price}
                  </div>
                </div>
                <div className="p-3 flex-1 flex flex-col justify-between">
                  <h3 className="text-xs font-bold text-neutral-900 line-clamp-2 h-8 leading-normal group-hover:text-neutral-800 transition-colors">
                    {p.title}
                  </h3>
                  <div className="flex justify-between items-center mt-3 pt-2.5 border-t border-primary-subtle">
                    <span className="text-[10px] text-neutral-500 font-semibold">{p.category_name}</span>
                    <span className="text-[10px] text-accent font-bold group-hover:underline">详情 &rarr;</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* 4. Educational Trust Indicators */}
      <div className="bg-white rounded-xl border border-primary-faint p-8">
        <h2 className="text-xs font-mono tracking-widest text-neutral-500 uppercase text-center mb-8">
          校内安全极简交易守则
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="text-left">
            <div className="w-8 h-8 rounded-lg bg-accent text-neutral-800 flex items-center justify-center font-bold mb-3">
              <Leaf className="h-4.5 w-4.5" />
            </div>
            <h3 className="font-extrabold text-xs text-neutral-900 mb-1">绿色流转</h3>
            <p className="text-[11px] text-neutral-500 font-medium leading-relaxed">
              支持本校课本教材及数码配件多次循环利用，倡导极简绿色的消费态度，避免过度浪费。
            </p>
          </div>

          <div className="text-left">
            <div className="w-8 h-8 rounded-lg bg-accent text-neutral-800 flex items-center justify-center font-bold mb-3">
              <ShieldCheck className="h-4.5 w-4.5" />
            </div>
            <h3 className="font-extrabold text-xs text-neutral-900 mb-1">当面核验</h3>
            <p className="text-[11px] text-neutral-500 font-medium leading-relaxed">
              本校零服务佣金。极力提倡买卖双方在校内当面交接和核实质量，消除线上欺诈隐患。
            </p>
          </div>

          <div className="text-left">
            <div className="w-8 h-8 rounded-lg bg-accent text-neutral-800 flex items-center justify-center font-bold mb-3">
              <Users className="h-4.5 w-4.5" />
            </div>
            <h3 className="font-extrabold text-xs text-neutral-900 mb-1">实名安全</h3>
            <p className="text-[11px] text-neutral-500 font-medium leading-relaxed">
              纯净校内用户管理，拒绝外部低端商业广告。保障本校安全、清静流畅的互动交易空间。
            </p>
          </div>
        </div>
      </div>
    </Layout>
  )
}
