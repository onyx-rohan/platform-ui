import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

export function LoginRedirect() {
  const { openLogin } = useAuthLayout()
  const navigate = useNavigate()

  useEffect(() => {
    openLogin()
    navigate('/', { replace: true })
  }, [])

  return null
}

export function SignUpRedirect() {
  const { openSignUp } = useAuthLayout()
  const navigate = useNavigate()

  useEffect(() => {
    openSignUp()
    navigate('/', { replace: true })
  }, [])

  return null
}