import { Navigate, Outlet } from 'react-router-dom'
import { useCurrentUser } from '@/hooks/useCurrentUser'

export default function AdminRoute() {
  const { data: user, isLoading } = useCurrentUser()
  if (isLoading) return null
  if (user?.role !== 'ADMIN' && user?.role !== 'SUPER') return <Navigate to="/" replace />
  return <Outlet />
}