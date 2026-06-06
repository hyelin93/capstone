import { apiClient } from '../../shared/api/client'
import type { Notice } from './types'

// GET /notices : 공지 목록 조회 (category 로 필터링)
export async function fetchNotices(category?: string): Promise<Notice[]> {
  const response = await apiClient.get<Notice[]>('/notices', {
    params: category ? { category } : undefined,
  })
  return response.data
}

// GET /notices/{id} : 공지 상세 조회
export async function fetchNotice(id: number): Promise<Notice> {
  const response = await apiClient.get<Notice>(`/notices/${id}`)
  return response.data
}
