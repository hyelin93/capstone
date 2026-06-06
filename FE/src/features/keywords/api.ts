import { apiClient } from '../../shared/api/client'
import type { Keyword } from './types'

// GET /keywords : 키워드 목록 조회
export async function fetchKeywords(): Promise<Keyword[]> {
  const response = await apiClient.get<Keyword[]>('/keywords')
  return response.data
}

// POST /keywords : 키워드 등록 (BE 는 body 의 "word" 키를 읽음)
export async function createKeyword(word: string): Promise<Keyword> {
  const response = await apiClient.post<Keyword>('/keywords', { word })
  return response.data
}

// DELETE /keywords/{id} : 키워드 삭제
export async function deleteKeyword(id: number): Promise<void> {
  await apiClient.delete(`/keywords/${id}`)
}
