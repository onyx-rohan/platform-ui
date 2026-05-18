import { useState } from 'react'
import { resetPassword } from 'aws-amplify/auth'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

const schema = z.object({
  email: z.email({ message: 'Please enter a valid email address' }),
})

type FormValues = z.infer<typeof schema>

export default function ForgotPasswordForm() {
  const { openLogin, openOTP } = useAuthLayout()
  const [isLoading, setIsLoading] = useState(false)

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '' },
  })

  async function onSubmit(values: FormValues) {
    setIsLoading(true)
    try {
      await resetPassword({ username: values.email })
      openOTP(values.email, 'RESET_PASSWORD')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to send reset code'
      toast.error('Reset failed', { description: message })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card className="auth-card">
      <CardHeader className="auth-card-header">
        <CardTitle className="auth-card-title">Forgot Password</CardTitle>
        <CardDescription>
          Enter your email and we'll send you a code to reset your password.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="auth-form">
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input type="email" placeholder="you@example.com" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Sending...' : 'Send Reset Code'}
            </Button>
          </form>
        </Form>
      </CardContent>

      <CardFooter className="auth-card-footer">
        Remember your password?{' '}
        <button type="button" onClick={openLogin} className="auth-link">Login</button>
      </CardFooter>
    </Card>
  )
}