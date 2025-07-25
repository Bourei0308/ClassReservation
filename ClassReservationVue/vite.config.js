import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {

      '/api': {
        target: 'http://localhost:8080',  // 后端地址
        ws: true,  // 开启 WebSocket 代理
        changeOrigin: true,
      }
    }
  },
  define: {
    global: 'window',
  },
})  
