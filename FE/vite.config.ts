import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
// 백엔드(localhost:8080)에 CORS 설정이 없어, dev 서버에서 같은 출처로 프록시해 CORS 회피.
// API 요청만 /api 접두어로 분리해 SPA 라우트(/notices 등) 새로고침과 충돌하지 않게 한다.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
