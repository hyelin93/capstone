import { useQuery } from '@tanstack/react-query'
import { fetchNotice, fetchNotices } from './api'
import type { Notice } from './types'

const NOTICE_CACHE_TTL = 5 * 60 * 1000
const NOTICE_CACHE_GC_TIME = 30 * 60 * 1000

interface NoticeCachePayload {
  savedAt: number
  data: Notice[]
}

export const noticeKeys = {
  all: ['notices'] as const,
  list: (category?: string) => [...noticeKeys.all, 'list', category ?? 'all'] as const,
  detail: (id: number) => [...noticeKeys.all, 'detail', id] as const,
}

function getNoticeCacheKey(category?: string) {
  return `notice-list:${category ?? 'all'}`
}

function readCachedNotices(category?: string): NoticeCachePayload | undefined {
  if (typeof window === 'undefined') return undefined

  try {
    const cached = window.sessionStorage.getItem(getNoticeCacheKey(category))
    if (!cached) return undefined

    const parsed = JSON.parse(cached) as NoticeCachePayload
    if (!Array.isArray(parsed.data) || typeof parsed.savedAt !== 'number') return undefined

    return parsed
  } catch {
    return undefined
  }
}

function writeCachedNotices(category: string | undefined, data: Notice[]) {
  if (typeof window === 'undefined') return

  try {
    window.sessionStorage.setItem(
      getNoticeCacheKey(category),
      JSON.stringify({ savedAt: Date.now(), data }),
    )
  } catch {
    // 저장 공간 제한 등 캐시 실패는 화면 동작에 영향을 주지 않는다.
  }
}

export function useNotices(category?: string) {
  const cachedNotices = readCachedNotices(category)

  return useQuery({
    queryKey: noticeKeys.list(category),
    queryFn: async () => {
      const notices = await fetchNotices(category)
      writeCachedNotices(category, notices)
      return notices
    },
    initialData: cachedNotices?.data,
    initialDataUpdatedAt: cachedNotices?.savedAt,
    staleTime: NOTICE_CACHE_TTL,
    gcTime: NOTICE_CACHE_GC_TIME,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    retry: 1,
  })
}

export function useNotice(id: number) {
  return useQuery({
    queryKey: noticeKeys.detail(id),
    queryFn: () => fetchNotice(id),
    enabled: Number.isFinite(id),
    staleTime: NOTICE_CACHE_TTL,
    gcTime: NOTICE_CACHE_GC_TIME,
    refetchOnWindowFocus: false,
    retry: 1,
  })
}
