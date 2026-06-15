import { useQuery } from '@tanstack/react-query'
import { fetchNotice, fetchNotices } from './api'

export const noticeKeys = {
  all: ['notices'] as const,
  list: (category?: string) => [...noticeKeys.all, 'list', category ?? 'all'] as const,
  detail: (id: number) => [...noticeKeys.all, 'detail', id] as const,
}

export function useNotices(category?: string) {
  return useQuery({
    queryKey: noticeKeys.list(category),
    queryFn: () => fetchNotices(category),
  })
}

export function useNotice(id: number) {
  return useQuery({
    queryKey: noticeKeys.detail(id),
    queryFn: () => fetchNotice(id),
    enabled: Number.isFinite(id),
  })
}
