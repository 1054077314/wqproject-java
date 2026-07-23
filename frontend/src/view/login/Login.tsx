import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'motion/react'
import {
  Eye,
  EyeOff,
  User,
  Lock,
  ArrowRight
} from 'lucide-react'
import request from '../../utils/request'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import type { ApiRes, User as UserType } from '../../types'

function getErrorMessage(error: any, fallback: string) {
  if (typeof error?.message === 'string') return error.message
  if (error?.data && typeof error.data === 'object') {
    const messages = Object.values(error.data)
      .flatMap((value) => Array.isArray(value) ? value : [value])
      .filter(Boolean)
      .map(String)
    if (messages.length > 0) return messages.join('; ')
  }
  return fallback
}

export default function Login() {
  const [isRegister, setIsRegister] = useState(false)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const navigate = useNavigate()
  const { login } = useAuth()
  const { toast } = useToast()

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setErrorMsg('')
    setLoading(true)

    try {
      if (isRegister) {
        await request.post('/register', { username, password })
        toast('注册成功！请使用该账号登录。', 'success')
        setIsRegister(false)
        setPassword('')
        setErrorMsg('')
        setLoading(false)
        return
      }

      const res: ApiRes<{ token: string; user: UserType }> = await request.post('/login', { username, password })
      login(res.data.token, res.data.user)
      toast(`欢迎回来 ${res.data.user.username}`, 'success')
      navigate('/', { replace: true })
    } catch (e: any) {
      const errMsg = getErrorMessage(e, isRegister ? '注册失败' : '登录失败')
      setErrorMsg(errMsg)
      toast(errMsg, 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col justify-between items-center py-10 px-6 font-sans text-neutral-900 select-none relative overflow-hidden">

      {/* Ambient background blur elements with custom slow floating animation properties */}
      <div className="absolute top-[-10%] left-1/2 -translate-x-1/2 w-[700px] h-[700px] bg-[radial-gradient(circle_at_center,rgba(220,220,220,0.55)_0,transparent_75%)] pointer-events-none rounded-full blur-3xl" />
      <div className="absolute -bottom-20 -left-20 w-[400px] h-[400px] bg-[radial-gradient(circle_at_center,rgba(240,240,240,0.7)_0,transparent_70%)] pointer-events-none rounded-full blur-3xl" />

      {/* Warm minimalist header tag */}
      <div className="w-full max-w-[380px] flex justify-center pt-4 z-10">
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1, duration: 0.4 }}
          className="inline-flex items-center gap-2 px-3 py-1 bg-white border border-neutral-200/50 rounded-full shadow-[0_2px_8px_rgba(0,0,0,0.02)]"
        >
          <span className="w-1.5 h-1.5 rounded-full bg-neutral-900 animate-ping" />
          <span className="text-[9px] font-bold tracking-wider uppercase font-mono text-neutral-500">
            校园闲置交易
          </span>
        </motion.div>
      </div>

      {/* Main Form Container */}
      <main className="w-full max-w-[380px] my-auto z-10">
        <motion.div
          initial={{ opacity: 0, y: 25, scale: 0.98 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          className="bg-white rounded-[28px] border border-neutral-200/70 p-8 sm:p-9 shadow-[0_12px_40px_rgba(0,0,0,0.02),_0_1px_3px_rgba(0,0,0,0.01)] hover:shadow-[0_20px_50px_rgba(0,0,0,0.04)] transition-all duration-500 relative"
        >

          <AnimatePresence mode="wait">
            <motion.div
              key={isRegister ? 'reg-hdr' : 'log-hdr'}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 10 }}
              transition={{ duration: 0.2 }}
              className="mb-8"
            >
              {/* Fun/Playful Dynamic Mascot Logo with magnetic-like cursor hover feel */}
              <motion.div
                whileHover={{ scale: 1.08, rotate: 6 }}
                whileTap={{ scale: 0.94 }}
                transition={{ type: "spring", stiffness: 450, damping: 15 }}
                className="w-12 h-12 bg-neutral-900 text-white rounded-[18px] mb-5 flex items-center justify-center font-black text-base select-none shadow-md cursor-pointer"
              >
                闲
              </motion.div>

              <h2 className="text-xl font-bold tracking-tight text-neutral-900 leading-none">
                {isRegister ? '加入闲置流转大厅' : '欢迎回到大厅'}
              </h2>
              <p className="text-[11px] text-neutral-400 mt-2 leading-relaxed font-semibold">
                {isRegister
                  ? '注册你的校内专属账户，轻松开启闲置循环'
                  : '请输入你的用户名和密码，立即发现同校好物'
                }
              </p>
            </motion.div>
          </AnimatePresence>

          {errorMsg && (
            <motion.div
              initial={{ opacity: 0, y: 4 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-neutral-50/80 border border-neutral-200/80 p-3 rounded-xl mb-6 flex items-start gap-2.5 text-left"
            >
              <div className="w-4 h-4 rounded-full bg-neutral-900 text-white flex items-center justify-center text-[9px] font-black shrink-0 mt-0.5">
                !
              </div>
              <p className="text-[11px] text-neutral-600 font-bold leading-relaxed">
                {errorMsg}
              </p>
            </motion.div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">

            {/* Username input box with smart focus scale properties */}
            <div className="space-y-1.5 text-left">
              <label className="block text-[10px] font-black text-neutral-400 uppercase tracking-wider pl-0.5">
                用户名
              </label>
              <div className="relative">
                <span className="absolute left-4 top-3.5 text-neutral-400 group-focus-within:text-neutral-900 transition-colors">
                  <User className="h-4 w-4 stroke-[2]" />
                </span>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  placeholder="请输入您的常用代号"
                  minLength={3}
                  maxLength={50}
                  autoComplete="username"
                  className="w-full pl-11 pr-4 py-3 bg-[#F9F9FB] border border-neutral-200/80 rounded-xl focus:outline-hidden focus:border-neutral-900 focus:bg-white focus:ring-1 focus:ring-neutral-900/10 transition-all duration-200 text-xs font-semibold text-neutral-800 placeholder:text-neutral-400"
                />
              </div>
            </div>

            {/* Password input box */}
            <div className="space-y-1.5 text-left">
              <label className="block text-[10px] font-black text-neutral-400 uppercase tracking-wider pl-0.5">
                访问密码
              </label>
              <div className="relative">
                <span className="absolute left-4 top-3.5 text-neutral-400 group-focus-within:text-neutral-900 transition-colors">
                  <Lock className="h-4 w-4 stroke-[2]" />
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="不少于 8 位安全密码"
                  minLength={8}
                  autoComplete="current-password"
                  className="w-full pl-11 pr-11 py-3 bg-[#F9F9FB] border border-neutral-200/80 rounded-xl focus:outline-hidden focus:border-neutral-900 focus:bg-white focus:ring-1 focus:ring-neutral-900/10 transition-all duration-200 text-xs font-semibold text-neutral-800 placeholder:text-neutral-400"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3.5 text-neutral-400 hover:text-neutral-950 transition-colors p-0.5 cursor-pointer"
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 stroke-[2]" />
                  ) : (
                    <Eye className="h-4 w-4 stroke-[2]" />
                  )}
                </button>
              </div>
            </div>

            {/* Submit button with smooth interactive click states */}
            <motion.button
              type="submit"
              disabled={loading}
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.98 }}
              className="w-full bg-neutral-900 hover:bg-black text-white font-bold text-xs py-3.5 rounded-xl transition-all duration-250 disabled:opacity-50 cursor-pointer flex items-center justify-center gap-2 mt-5 shadow-[0_4px_12px_rgba(0,0,0,0.03)] hover:shadow-[0_4px_20px_rgba(0,0,0,0.08)]"
            >
              <span>{loading ? '请稍候...' : isRegister ? '轻按创建账户' : '快捷登录'}</span>
              {!loading && (
                <motion.span
                  animate={{ x: [0, 4, 0] }}
                  transition={{ repeat: Infinity, duration: 1.5, ease: "easeInOut" }}
                >
                  <ArrowRight className="h-3.5 w-3.5 stroke-[2.5]" />
                </motion.span>
              )}
            </motion.button>

            {/* Alternating flow selection toggle */}
            <div className="pt-4 border-t border-neutral-100 text-center select-none mt-2">
              <p className="text-[11px] text-neutral-400 font-bold leading-relaxed">
                {isRegister ? '已经有大厅账号了？' : '还没有注册账号吗？'}
                <button
                  type="button"
                  className="text-neutral-900 hover:underline font-extrabold ml-1 cursor-pointer"
                  onClick={() => {
                    setIsRegister(!isRegister)
                    setErrorMsg('')
                    setPassword('')
                  }}
                >
                  {isRegister ? '点此登录' : '立即注册'}
                </button>
              </p>
            </div>

          </form>

        </motion.div>
      </main>

      {/* Styled Subtle Footer with neutral branding */}
      <footer className="w-full max-w-[380px] text-center text-[9px] text-neutral-400 font-bold tracking-wider select-none z-10 leading-relaxed">
        &copy; 2026 校园二手闲置商品流转大厅
      </footer>

    </div>
  )
}
