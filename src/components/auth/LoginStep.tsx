import { useState } from 'react'
import { signIn, resendSignUpCode } from 'aws-amplify/auth'
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
  password: z.string().min(1, { message: 'Password is required' }),
})

type LoginFormValues = z.infer<typeof schema>

export default function LoginStep() {
  const { closeAuthLayout, goToStep } = useAuthLayout()
  const [isLoading, setIsLoading] = useState(false)

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', password: '' },
  })

  async function onSubmit(loginForm: LoginFormValues) {
    setIsLoading(true)
    try {
      await signIn({ username: loginForm.email, password: loginForm.password })
      closeAuthLayout()
    } catch (err) {
      if (err instanceof Error && err.name === 'UserNotConfirmedException') {
        try {
          await resendSignUpCode({ username: loginForm.email })
          toast.info('Verify your email', { description: 'A new confirmation code has been sent.' })
          goToStep('otp', { email: loginForm.email, reason: 'SIGNUP' })
        } catch {
          toast.error('Login failed', { description: 'Account unconfirmed and resend failed. Please sign up again.' })
        }
      } else {
        const message = err instanceof Error ? err.message : 'Incorrect username or password'
        toast.error('Login failed', { description: message })
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl">Onyx Platform</CardTitle>
        <CardDescription>
          Enter your email and password to login to the Onyx Platform.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Password</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="••••••••" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Logging in...' : 'Login'}
            </Button>
          </form>
        </Form>
      </CardContent>

      <CardFooter className="flex justify-between text-sm">
        <button
          type="button"
          onClick={() => goToStep('forgot-password')}
          className="text-muted-foreground hover:text-primary hover:underline"
        >
          Forgot password?
        </button>
        <span className="text-muted-foreground">
          New here?{' '}
          <button
            type="button"
            onClick={() => goToStep('signup')}
            className="text-primary hover:underline"
          >
            Sign up
          </button>
        </span>
      </CardFooter>
    </Card>
  )
}
