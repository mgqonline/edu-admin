import { defineConfig } from 'vite'
import vue2 from '@vitejs/plugin-vue2'

export default defineConfig({
  plugins: [vue2()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        headers: { 'X-Perms': '*' }
      },
      '/wechat/api': {
        target: 'http://localhost:8090',
        changeOrigin: true
      }
    }
  }
})