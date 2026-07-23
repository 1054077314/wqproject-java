import { useState, useEffect, useCallback, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Search, X } from 'lucide-react'
import request from '../../utils/request'
import Layout from '../../components/layout/Layout'
import type { ProductListItem, Category, PaginatedRes } from '../../types'

export default function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [products, setProducts] = useState<ProductListItem[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)

  const page = Number(searchParams.get('page')) || 1
  const categoryId = searchParams.get('category_id') || ''
  const searchQuery = searchParams.get('search') || ''
  const pageSize = 20

  const [localSearch, setLocalSearch] = useState(searchQuery)

  // Sync input value when page params change
  useEffect(() => {
    setLocalSearch(searchQuery)
  }, [searchQuery])

  const loadProducts = useCallback(async () => {
    setLoading(true)
    try {
      const params: Record<string, string> = { page: String(page), page_size: String(pageSize) }
      if (categoryId) params.category_id = categoryId
      if (searchQuery) params.search = searchQuery
      const res: PaginatedRes<ProductListItem> = await request.get('/products/', { params })
      const nextProducts = Array.isArray(res.data?.results) ? res.data.results : []
      setProducts(nextProducts)
      setTotal(typeof res.data?.count === 'number' ? res.data.count : nextProducts.length)
    } catch {
      setProducts([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }, [page, categoryId, searchQuery])

  useEffect(() => {
    loadProducts()
  }, [loadProducts])

  useEffect(() => {
    request.get('/categories/').then((res: any) => {
      const nextCategories = Array.isArray(res?.data) ? res.data : []
      setCategories(Array.isArray(nextCategories) ? nextCategories : [])
    }).catch(() => {})
  }, [])

  const totalPages = Math.ceil(total / pageSize)

  function handleCategoryChange(id: string) {
    const params: Record<string, string> = {}
    if (id) params.category_id = id
    if (searchQuery) params.search = searchQuery
    params.page = '1'
    setSearchParams(params)
  }

  function handleSearchSubmit(e: FormEvent) {
    e.preventDefault()
    const params: Record<string, string> = {}
    if (categoryId) params.category_id = categoryId
    if (localSearch.trim()) params.search = localSearch.trim()
    params.page = '1'
    setSearchParams(params)
  }

  function handleClearSearch() {
    setLocalSearch('')
    const params: Record<string, string> = {}
    if (categoryId) params.category_id = categoryId
    params.page = '1'
    setSearchParams(params)
  }

  return (
    <Layout>
      {/* Search & Header Section */}
      <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between border-b border-primary-faint pb-6 select-none">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-neutral-900">
            闲置大厅
          </h1>
          <p className="text-xs text-neutral-500 mt-1 font-semibold leading-relaxed">发现真实好物，倡导极简安全流转氛围</p>
        </div>

        <form onSubmit={handleSearchSubmit} className="relative max-w-xs w-full">
          <input
            type="text"
            value={localSearch}
            onChange={e => setLocalSearch(e.target.value)}
            placeholder="搜索您想要的物品..."
            className="w-full pl-9 pr-8 py-2 border border-primary-faint rounded-xl focus:outline-hidden focus:border-primary text-xs text-neutral-800 font-semibold"
          />
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-neutral-500" />
          {localSearch && (
            <button
              type="button"
              onClick={handleClearSearch}
              className="absolute right-2.5 top-2.5 hover:bg-primary-subtle p-0.5 rounded-full transition-colors cursor-pointer"
            >
              <X className="h-3.5 w-3.5 text-neutral-500" />
            </button>
          )}
        </form>
      </div>

      {/* Categories Horizontal Tabs bar */}
      <div className="mb-8 flex flex-wrap gap-2 items-center select-none">
        <span className="text-[10px] text-neutral-500 font-bold uppercase tracking-widest mr-2">分类筛选:</span>
        <button
          onClick={() => handleCategoryChange('')}
          className={`px-3.5 py-1.5 rounded-full text-[11px] font-bold select-none transition-all cursor-pointer ripple-container ${
            !categoryId
              ? 'bg-primary text-white'
              : 'bg-white border border-primary-faint text-neutral-500 hover:text-neutral-900'
          }`}
        >
          全类别商品
        </button>
        {categories.map(c => (
          <button
            key={c.id}
            onClick={() => handleCategoryChange(String(c.id))}
            className={`px-3.5 py-1.5 rounded-full text-[11px] font-bold select-none transition-all cursor-pointer ripple-container ${
              categoryId === String(c.id)
                ? 'bg-primary text-white'
                : 'bg-white border border-primary-faint text-neutral-500 hover:text-neutral-900'
            }`}
          >
            {c.name}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center py-20 text-neutral-500 text-xs font-semibold">加载中...</div>
      ) : products.length === 0 ? (
        <div className="bg-white rounded-xl border border-primary-faint py-16 text-center px-6">
          <p className="text-neutral-500 text-xs font-bold tracking-wider">暂无匹配物品</p>
          <p className="text-xs text-neutral-500 mt-1">没有找到该分类或关键词下的二手商品。</p>
          {(searchQuery || categoryId) && (
            <button
              onClick={() => {
                setLocalSearch('')
                setSearchParams({})
              }}
              className="mt-5 bg-primary hover:bg-primary-light text-white text-xs px-4 py-2 rounded-xl font-bold transition-all cursor-pointer ripple-container"
            >
              重置全部条件
            </button>
          )}
        </div>
      ) : (
        <>
          {/* Main Pure Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-5">
            {products.map(p => (
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
                      className="w-full h-full object-cover group-hover:scale-102 transition-transform duration-300 pointer-events-none"
                    />
                  ) : (
                    <span className="text-neutral-400 text-xs">暂无图片</span>
                  )}
                </div>
                <div className="p-3.5 flex-1 flex flex-col justify-between">
                  <h3 className="text-xs font-bold text-neutral-900 line-clamp-2 h-8 leading-normal group-hover:text-neutral-800 transition-colors">
                    {p.title}
                  </h3>
                  <div className="flex justify-between items-end mt-4 pt-2.5 border-t border-primary-subtle">
                    <span className="text-neutral-900 font-extrabold text-xs">¥{p.price}</span>
                    <span className="text-[9px] text-neutral-500 font-bold uppercase tracking-wider">
                      {p.category_name}
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>

          {/* Simple Pagination Buttons */}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-12 select-none">
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(p => (
                <button
                  key={p}
                  onClick={() => {
                    const params: Record<string, string> = { page: String(p) }
                    if (categoryId) params.category_id = categoryId
                    if (searchQuery) params.search = searchQuery
                    setSearchParams(params)
                  }}
                  className={`w-8 h-8 rounded-lg text-xs font-bold cursor-pointer transition-all ripple-container ${
                    p === page
                      ? 'bg-primary text-white'
                      : 'bg-white border border-primary-faint text-neutral-500 hover:border-primary'
                  }`}
                >
                  {p}
                </button>
              ))}
            </div>
          )}
        </>
      )}
    </Layout>
  )
}
