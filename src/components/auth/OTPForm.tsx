import { useState, useEffect, useRef } from 'react'
import { confirmSignUp, resendSignUpCode, resetPassword } from 'aws-amplify/auth'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

const OTP_TTL = 120

const schema = z.object({
  code: z.string().length(6, 'Code must be 6 digits').regex(/^\d+$/, 'Digits only'),
})

type FormValues = z.infer<typeof schema>

function formatCountdown(seconds: number) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = (seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

export default function OTPForm({ email, reason }: { email: string; reason: 'SIGNUP' | 'RESET_PASSWORD' }) {
  const { openLogin, openChangePassword } = useAuthLayout()
  const [countdown, setCountdown] = useState(OTP_TTL)
  const [isLoading, setIsLoading] = useState(false)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const canResend = countdown === 0

  useEffect(() => {
    intervalRef.current = setInterval(() => {
      setCountdown(prev => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current)
    }
  }, [])

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { code: '' },
  })

  async function onSubmit(values: FormValues) {
    setIsLoading(true)
    try {
      if (reason === 'SIGNUP') {
        await confirmSignUp({ username: email, confirmationCode: values.code })
        toast.success('Email verified', { description: 'You can now log in.' })
        openLogin()
      } else {
        openChangePassword(email, values.code)
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Invalid code'
      toast.error('Verification failed', { description: message })
    } finally {
      setIsLoading(false)
    }
  }

  async function handleResend() {
    setIsLoading(true)
    try {
      if (reason === 'SIGNUP') {
        await resendSignUpCode({ username: email })
      } else {
        await resetPassword({ username: email })
      }
      toast.success('Code resent', { description: `A new code was sent to ${email}.` })
      setCountdown(OTP_TTL)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to resend code'
      toast.error('Resend failed', { description: message })
    } finally {
      setIsLoading(false)
    }
  }

  const maskedEmail = email.replace(/(.{2})(.*)(?=@)/, (_, a, b) => a + '*'.repeat(b.length))

  return (
    <Card className="auth-card">
      <CardHeader className="auth-card-header">
        <CardTitle className="auth-card-title">Enter Verification Code</CardTitle>
        <CardDescription>A 6-digit code was sent to {maskedEmail}.</CardDescription>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="auth-form">
            <FormField
              control={form.control}
              name="code"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Verification Code</FormLabel>
                  <FormControl>
                    <Input placeholder="123456" maxLength={6} inputMode="numeric" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Verifying...' : 'Submit'}
            </Button>

            <Button
              type="button"
              variant="outline"
              className="w-full"
              disabled={!canResend || isLoading}
              onClick={handleResend}
            >
              {isLoading && canResend ? 'Sending...' : canResend ? 'Resend' : `Resend (${formatCountdown(countdown)})`}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}