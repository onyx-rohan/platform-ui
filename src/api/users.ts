import type { User, UserType } from '@/types'
import apiClient from './client'

export interface CreateUserPayload {
  firstName: string
  lastName: string
  email: string
  phone: string
  country: string
  userType: UserType
  businessOwner?: boolean
  businessMember?: boolean
}

export async function getCurrentUser(): Promise<User> {
  const { data } = await apiClient.get<User>('/api/user/me')
  return data
}

export async function createUser(payload: CreateUserPayload): Promise<User> {
  const { data } = await apiClient.post<User>('/api/user', payload)
  return data
}