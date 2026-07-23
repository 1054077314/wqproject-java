import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
// @ts-expect-error — @tailwindcss/vite 暂无类型声明
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      // Java Spring Boot API
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/media': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
    },
  },
})
