import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

export function LoginRedirect() {
  const { openLoginModal } = useAuthLayout()
  const navigate = useNavigate()

  useEffect(() => {
    openLoginModal()
    navigate('/', { replace: true })
  }, [])

  return null
}

export function SignUpRedirect() {
  const { openSignUpModal } = useAuthLayout()
  const navigate = useNavigate()

  useEffect(() => {
    openSignUpModal()
    navigate('/', { replace: true })
  }, [])

  return null
}