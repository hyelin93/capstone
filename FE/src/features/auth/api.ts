import { apiClient } from '../../shared/api/client'
import type { LoginRequest, SignupRequest } from './types'

// POST /users/signup : 회원가입 (BE 는 결과 메시지를 문자열로 반환)
export async function signup(payload: SignupRequest): Promise<string> {
  const response = await apiClient.post<string>('/users/signup', payload)
  return response.data
}

// POST /users/login : 로그인 (BE 는 결과 메시지를 문자열로 반환)
export async function login(payload: LoginRequest): Promise<string> {
  const response = await apiClient.post<string>('/users/login', payload)
  return response.data
}
