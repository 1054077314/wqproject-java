import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { motion, AnimatePresence } from 'motion/react'
import { CheckCircle, AlertTriangle, AlertCircle, Info, X } from 'lucide-react'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastItem {
  id: string
  message: string
  type: ToastType
}

interface ConfirmOptions {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  onConfirm: () => void
  onCancel?: () => void
}

interface ToastContextType {
  toast: (message: string, type?: ToastType) => void
  confirm: (options: ConfirmOptions) => void
}

const ToastContext = createContext<ToastContextType | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])
  const [confirmConfig, setConfirmConfig] = useState<ConfirmOptions | null>(null)

  const toast = useCallback((message: string, type: ToastType = 'info') => {
    const id = Math.random().toString(36).substring(2, 9)
    setToasts(prev => [...prev, { id, message, type }])
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
      setToasts(prev => prev.filter(item => item.id !== id))
    }, 4000)
  }, [])

  const confirm = useCallback((options: ConfirmOptions) => {
    setConfirmConfig(options)
  }, [])

  const handleConfirmAction = () => {
    if (confirmConfig) {
      confirmConfig.onConfirm()
      setConfirmConfig(null)
    }
  }

  const handleConfirmCancel = () => {
    if (confirmConfig) {
      if (confirmConfig.onCancel) confirmConfig.onCancel()
      setConfirmConfig(null)
    }
  }

  return (
    <ToastContext.Provider value={{ toast, confirm }}>
      {children}

      {/* Floating Toasts container */}
      <div className="fixed top-5 right-5 z-55 flex flex-col gap-2.5 max-w-sm w-full pointer-events-none select-none">
        <AnimatePresence>
          {toasts.map(t => (
            <motion.div
              key={t.id}
              initial={{ opacity: 0, y: -20, scale: 0.9 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, scale: 0.85, transition: { duration: 0.15 } }}
              className="pointer-events-auto bg-white/95 backdrop-blur-md rounded-xl p-4 shadow-xl border border-gray-150 flex gap-3 items-start"
            >
              <div className="shrink-0 mt-0.5">
                {t.type === 'success' && <CheckCircle className="h-5 w-5 text-emerald-500" />}
                {t.type === 'error' && <AlertCircle className="h-5 w-5 text-red-500" />}
                {t.type === 'warning' && <AlertTriangle className="h-5 w-5 text-yellow-500" />}
                {t.type === 'info' && <Info className="h-5 w-5 text-blue-500" />}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gray-800 font-extrabold leading-normal whitespace-pre-wrap">
                  {t.message}
                </p>
              </div>
              <button
                onClick={() => setToasts(prev => prev.filter(item => item.id !== t.id))}
                className="shrink-0 text-gray-400 hover:text-gray-600 rounded-lg p-0.5 hover:bg-gray-150 transition-colors"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>

      {/* Custom confirm modal overlay */}
      <AnimatePresence>
        {confirmConfig && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            {/* Backdrop */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={handleConfirmCancel}
              className="absolute inset-0 bg-black/60 backdrop-blur-xs"
            />

            {/* Modal Body */}
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              transition={{ type: 'spring', duration: 0.35, bounce: 0.2 }}
              className="relative bg-white max-w-sm w-full rounded-2xl p-6 shadow-2xl border border-gray-100"
            >
              <div className="flex gap-4">
                <div className="h-10 w-10 rounded-full bg-red-50 flex items-center justify-center shrink-0">
                  <AlertTriangle className="h-5 w-5 text-red-500" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="text-sm font-black text-gray-950 mb-1 leading-normal">
                    {confirmConfig.title || '安全提示'}
                  </h3>
                  <p className="text-xs text-gray-500 leading-relaxed font-semibold">
                    {confirmConfig.message}
                  </p>
                </div>
              </div>

              <div className="flex justify-end gap-2.5 mt-6 font-extrabold text-xs">
                <button
                  type="button"
                  onClick={handleConfirmCancel}
                  className="px-4 py-2 border border-gray-200 text-gray-600 rounded-lg hover:bg-gray-50 transition-all cursor-pointer"
                >
                  {confirmConfig.cancelText || '取消'}
                </button>
                <button
                  type="button"
                  onClick={handleConfirmAction}
                  className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-all cursor-pointer shadow-xs"
                >
                  {confirmConfig.confirmText || '确认'}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) throw new Error('useToast must be used within ToastProvider')
  return context
}
