import { apiClient } from '../../shared/api/client'
import type { Notice } from './types'

export interface FetchNoticesParams {
  category?: string
  keyword?: string
  page?: number
  size?: number
}

// GET /notices : 공지 목록 조회 (category 로 필터링)
export async function fetchNotices(params?: FetchNoticesParams): Promise<Notice[]> {
  const response = await apiClient.get<Notice[]>('/notices', {
    params,
  })
  return response.data
}

// GET /notices/{id} : 공지 상세 조회
export async function fetchNotice(id: number): Promise<Notice> {
  const response = await apiClient.get<Notice>(`/notices/${id}`)
  return response.data
}
