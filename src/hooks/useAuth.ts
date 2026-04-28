import { useState, useEffect } from 'react'
import { getCurrentUser, signOut } from 'aws-amplify/auth'
import { Hub } from 'aws-amplify/utils'
import type { AuthUser } from 'aws-amplify/auth'

export function useAuth() {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getCurrentUser()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false))

    const unsubscribe = Hub.listen('auth', ({ payload }) => {
      if (payload.event === 'signedIn') {
        getCurrentUser().then(setUser).catch(() => setUser(null))
      } else if (payload.event === 'signedOut') {
        setUser(null)
      }
    })

    return unsubscribe
  }, [])

  return {
    user,
    loading,
    isAuthenticated: user !== null,
    signOut: () => signOut(),
  }
}