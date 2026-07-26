import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * 前台 HR 端（Vite 5173 端口）
 * - 页面跑在 5173
 * - /api/**   代理到 Spring Boot 8080（调用 /public/*）
 * - /files/** 代理上传目录（后端静态映射 /files/**）
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
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
