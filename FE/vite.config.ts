import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
// 백엔드(localhost:8080)에 CORS 설정이 없어, dev 서버에서 같은 출처로 프록시해 CORS 회피.
// baseURL 을 상대경로('')로 두면 모든 요청이 vite dev 서버를 거쳐 8080 으로 전달됨.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/notices': 'http://localhost:8080',
      '/keywords': 'http://localhost:8080',
      '/users': 'http://localhost:8080',
      '/notifications': 'http://localhost:8080',
    },
  },
})
