import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * 后台管理前端（Vite 5174 端口）
 * - 页面跑在 5174
 * - /api/**   代理到 Spring Boot 8080
 * - /files/** 代理上传目录（后端静态映射）
 * 后端跑在 localhost:8080，避免 CORS 预检
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/files': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
