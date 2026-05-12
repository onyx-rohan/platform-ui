import { createContext, useContext, useState } from 'react'
import type { ReactNode } from 'react'
import LoginStep from '@/components/auth/LoginStep'
import SignUpStep from '@/components/auth/SignUpStep'
import ForgotPasswordStep from '@/components/auth/ForgotPasswordStep'
import OTPStep from '@/components/auth/OTPStep'
import ChangePasswordStep from '@/components/auth/ChangePasswordStep'

type AuthStep = 'login' | 'signup' | 'forgot-password' | 'otp' | 'change-password'

interface AuthStepData {
  email: string
  reason: 'SIGNUP' | 'RESET_PASSWORD'
  confirmationCode: string
}

interface AuthLayoutContextValue {
  openLoginModal: () => void
  openSignUpModal: () => void
  openForgotPasswordModal: () => void
  closeAuthLayout: () => void
  goToStep: (step: AuthStep, data?: Partial<AuthStepData>) => void
  stepData: Partial<AuthStepData>
}

const AuthLayoutContext = createContext<AuthLayoutContextValue | null>(null)

export function useAuthLayout() {
  const ctx = useContext(AuthLayoutContext)
  if (!ctx) throw new Error('useAuthLayout must be used within AuthLayout')
  return ctx
}

export default function AuthLayout({ children }: { children: ReactNode }) {
  const [isOpen, setIsOpen] = useState(false)
  const [currentStep, setCurrentStep] = useState<AuthStep>('login')
  const [stepData, setStepData] = useState<Partial<AuthStepData>>({})

  function openLoginModal() {
    setStepData({})
    setCurrentStep('login')
    setIsOpen(true)
  }

  function openSignUpModal() {
    setStepData({})
    setCurrentStep('signup')
    setIsOpen(true)
  }

  function openForgotPasswordModal() {
    setStepData({})
    setCurrentStep('forgot-password')
    setIsOpen(true)
  }

  function closeAuthLayout() {
    setIsOpen(false)
    setStepData({})
  }

  function goToStep(step: AuthStep, data?: Partial<AuthStepData>) {
    if (data) setStepData(prev => ({ ...prev, ...data }))
    setCurrentStep(step)
  }

  return (
    <AuthLayoutContext.Provider value={{
      openLoginModal,
      openSignUpModal,
      openForgotPasswordModal,
      closeAuthLayout,
      goToStep,
      stepData,
    }}>
      {children}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div
            className="fixed inset-0 bg-background/80 backdrop-blur-sm"
            onClick={closeAuthLayout}
          />
          <div className="relative z-50 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            {currentStep === 'login' && <LoginStep />}
            {currentStep === 'signup' && <SignUpStep />}
            {currentStep === 'forgot-password' && <ForgotPasswordStep />}
            {currentStep === 'otp' && <OTPStep />}
            {currentStep === 'change-password' && <ChangePasswordStep />}
          </div>
        </div>
      )}
    </AuthLayoutContext.Provider>
  )
}
