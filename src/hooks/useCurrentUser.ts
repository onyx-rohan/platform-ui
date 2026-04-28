import { useQuery } from '@tanstack/react-query'
import { useAuth } from './useAuth'
import apiClient from '@/api/client'
import type { User } from '@/types'

export function useCurrentUser() {
  const { isAuthenticated } = useAuth()
  return useQuery({
    queryKey: ['currentUser'],
    queryFn: () => apiClient.get<User>('/api/user/me').then(r => r.data),
    enabled: isAuthenticated,
  })
}