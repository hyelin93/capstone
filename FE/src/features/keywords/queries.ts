import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createKeyword, deleteKeyword, fetchKeywords } from './api'

export const keywordKeys = {
  all: ['keywords'] as const,
}

export function useKeywords() {
  return useQuery({
    queryKey: keywordKeys.all,
    queryFn: fetchKeywords,
  })
}

export function useCreateKeyword() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createKeyword,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: keywordKeys.all })
    },
  })
}

export function useDeleteKeyword() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteKeyword,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: keywordKeys.all })
    },
  })
}
