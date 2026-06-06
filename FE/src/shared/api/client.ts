import axios from 'axios'

// 모든 API 요청이 공유하는 axios 인스턴스. baseURL 은 .env 의 VITE_API_BASE_URL 사용.
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})
