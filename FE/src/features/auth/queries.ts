import { useMutation } from '@tanstack/react-query'
import { login, signup } from './api'

export function useSignup() {
  return useMutation({ mutationFn: signup })
}

export function useLogin() {
  return useMutation({ mutationFn: login })
}
