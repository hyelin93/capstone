import { useInfiniteQuery, useQuery } from '@tanstack/react-query'
import { fetchNotice, fetchNotices } from './api'
import type { Notice } from './types'

export const NOTICE_PAGE_SIZE = 20
const NOTICE_CACHE_TTL = 30 * 60 * 1000
const NOTICE_CACHE_GC_TIME = 30 * 60 * 1000

interface NoticeCachePayload {
  savedAt: number
  data: Notice[]
}

export const noticeKeys = {
  all: ['notices'] as const,
  list: (category?: string) => [...noticeKeys.all, 'list', category ?? 'all'] as const,
  infiniteList: (category?: string) => [...noticeKeys.all, 'infinite-list', category ?? 'all'] as const,
  detail: (id: number) => [...noticeKeys.all, 'detail', id] as const,
}

function getNoticeCacheKey(category?: string) {
  return `notice-list:${category ?? 'all'}`
}

function readCachedNotices(category?: string): NoticeCachePayload | undefined {
  if (typeof window === 'undefined') return undefined

  try {
    const cacheKey = getNoticeCacheKey(category)
    const cached = window.sessionStorage.getItem(cacheKey) ?? window.localStorage.getItem(cacheKey)
    if (!cached) return undefined

    const parsed = JSON.parse(cached) as NoticeCachePayload
    if (!Array.isArray(parsed.data) || typeof parsed.savedAt !== 'number') return undefined

    return {
      savedAt: parsed.savedAt,
      data: parsed.data.slice(0, NOTICE_PAGE_SIZE),
    }
  } catch {
    return undefined
  }
}

function writeCachedNotices(category: string | undefined, data: Notice[]) {
  if (typeof window === 'undefined') return

  try {
    const cacheKey = getNoticeCacheKey(category)
    const cacheValue = JSON.stringify({ savedAt: Date.now(), data: data.slice(0, NOTICE_PAGE_SIZE) })
    window.sessionStorage.setItem(cacheKey, cacheValue)
    window.localStorage.setItem(cacheKey, cacheValue)
  } catch {
    // 저장 공간 제한 등 캐시 실패는 화면 동작에 영향을 주지 않는다.
  }
}

export function useNotices(category?: string) {
  const cachedNotices = readCachedNotices(category)

  return useQuery({
    queryKey: noticeKeys.list(category),
    queryFn: async () => {
      const notices = await fetchNotices({ category, page: 0, size: NOTICE_PAGE_SIZE })
      writeCachedNotices(category, notices)
      return notices
    },
    initialData: cachedNotices?.data,
    initialDataUpdatedAt: cachedNotices?.savedAt,
    staleTime: NOTICE_CACHE_TTL,
    gcTime: NOTICE_CACHE_GC_TIME,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    refetchOnMount: false,
    retry: 1,
  })
}

export function useInfiniteNotices(category?: string) {
  const cachedNotices = readCachedNotices(category)

  return useInfiniteQuery({
    queryKey: noticeKeys.infiniteList(category),
    initialPageParam: 0,
    queryFn: async ({ pageParam }) => {
      const page = Number(pageParam)
      const notices = await fetchNotices({ category, page, size: NOTICE_PAGE_SIZE })

      if (page === 0) {
        writeCachedNotices(category, notices)
      }

      return notices
    },
    getNextPageParam: (lastPage, _pages, lastPageParam) => {
      if (lastPage.length < NOTICE_PAGE_SIZE) return undefined
      return Number(lastPageParam) + 1
    },
    initialData: cachedNotices
      ? {
          pages: [cachedNotices.data],
          pageParams: [0],
        }
      : undefined,
    initialDataUpdatedAt: cachedNotices?.savedAt,
    staleTime: NOTICE_CACHE_TTL,
    gcTime: NOTICE_CACHE_GC_TIME,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    refetchOnMount: false,
    retry: 1,
  })
}

export function useNotice(id: number, initialNotice?: Notice) {
  return useQuery({
    queryKey: noticeKeys.detail(id),
    queryFn: () => fetchNotice(id),
    enabled: Number.isFinite(id),
    placeholderData: initialNotice,
    staleTime: NOTICE_CACHE_TTL,
    gcTime: NOTICE_CACHE_GC_TIME,
    refetchOnWindowFocus: false,
    retry: 1,
  })
}
