import { createContext, useContext, useState } from 'react'
import type { ReactNode } from 'react'
import LoginForm from '@/components/auth/login/LoginForm'
import SignUpForm from '@/components/auth/signup/SignUpForm'
import ForgotPasswordForm from '@/components/auth/forgot_password/ForgotPasswordForm'
import OTPForm from '@/components/auth/OTPForm'
import ChangePasswordForm from '@/components/auth/forgot_password/ChangePasswordForm'

type ActiveView =
  | { type: 'login' }
  | { type: 'signup' }
  | { type: 'forgot-password' }
  | { type: 'otp'; email: string; reason: 'SIGNUP' | 'RESET_PASSWORD' }
  | { type: 'change-password'; email: string; confirmationCode: string }
  | null

interface AuthContext {
  openLogin: () => void
  openSignUp: () => void
  openForgotPassword: () => void
  openOTP: (email: string, reason: 'SIGNUP' | 'RESET_PASSWORD') => void
  openChangePassword: (email: string, confirmationCode: string) => void
  close: () => void
}

const AuthLayoutContext = createContext<AuthContext | null>(null)

export function useAuthLayout() {
  const ctx = useContext(AuthLayoutContext)
  if (!ctx) throw new Error('useAuthLayout must be used within AuthLayout')
  return ctx
}

export default function AuthLayout({ children }: { children: ReactNode }) {
  const [activeView, setActiveView] = useState<ActiveView>(null)

  const openLogin = () => setActiveView({ type: 'login' })
  const openSignUp = () => setActiveView({ type: 'signup' })
  const openForgotPassword = () => setActiveView({ type: 'forgot-password' })
  const openOTP = (email: string, reason: 'SIGNUP' | 'RESET_PASSWORD') => setActiveView({ type: 'otp', email, reason })
  const openChangePassword = (email: string, confirmationCode: string) => setActiveView({ type: 'change-password', email, confirmationCode })
  const close = () => setActiveView(null)

  return (
    <AuthLayoutContext.Provider value={{ openLogin, openSignUp, openForgotPassword, openOTP, openChangePassword, close }}>
      {children}
      {activeView && (
        <div className="auth-modal-overlay">
          <div className="auth-modal-backdrop" onClick={close} />
          <div className="auth-modal-content">
            {activeView.type === 'login' && <LoginForm />}
            {activeView.type === 'signup' && <SignUpForm />}
            {activeView.type === 'forgot-password' && <ForgotPasswordForm />}
            {activeView.type === 'otp' && <OTPForm email={activeView.email} reason={activeView.reason} />}
            {activeView.type === 'change-password' && <ChangePasswordForm email={activeView.email} confirmationCode={activeView.confirmationCode} />}
          </div>
        </div>
      )}
    </AuthLayoutContext.Provider>
  )
}