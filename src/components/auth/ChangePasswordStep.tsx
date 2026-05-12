import { useState } from 'react'
import { confirmResetPassword } from 'aws-amplify/auth'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { CheckCircle, XCircle } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

const schema = z.object({
  newPassword: z.string()
    .min(8, 'Min 8 characters')
    .max(32, 'Max 32 characters')
    .regex(/[A-Z]/, 'Must contain an uppercase letter')
    .regex(/[a-z]/, 'Must contain a lowercase letter')
    .regex(/[0-9]/, 'Must contain a number')
    .regex(/[#?!@$%^&*-]/, 'Must contain a special character'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine(data => data.newPassword === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
})

type ChangePasswordFormValues = z.infer<typeof schema>

const rules = [
  { label: 'Minimum 8 characters',          test: (v: string) => v.length >= 8 },
  { label: 'Maximum 32 characters',          test: (v: string) => v.length <= 32 && v.length > 0 },
  { label: 'At least one uppercase letter',  test: (v: string) => /[A-Z]/.test(v) },
  { label: 'At least one lowercase letter',  test: (v: string) => /[a-z]/.test(v) },
  { label: 'At least one number',            test: (v: string) => /[0-9]/.test(v) },
  { label: 'At least one special character', test: (v: string) => /[#?!@$%^&*-]/.test(v) },
]

export default function ChangePasswordStep() {
  const { goToStep, stepData } = useAuthLayout()
  const { email, confirmationCode } = stepData
  const [isLoading, setIsLoading] = useState(false)

  const form = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(schema),
    defaultValues: { newPassword: '', confirmPassword: '' },
  })

  const newPassword = form.watch('newPassword')
  const confirmPassword = form.watch('confirmPassword')

  const anyPasswordRuleFailing = newPassword.length > 0 && rules.some(rule => !rule.test(newPassword))
  const passwordsNotMatching = confirmPassword.length > 0 && newPassword !== confirmPassword

  async function onSubmit(values: ChangePasswordFormValues) {
    if (!email || !confirmationCode) {
      toast.error('Session expired', { description: 'Please restart the password reset flow.' })
      goToStep('forgot-password')
      return
    }
    setIsLoading(true)
    try {
      await confirmResetPassword({ username: email, confirmationCode, newPassword: values.newPassword })
      toast.success('Password changed', { description: 'You can now log in with your new password.' })
      goToStep('login')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to change password'
      toast.error('Change failed', { description: message })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl">Set New Password</CardTitle>
        <CardDescription>Choose a strong password for your account.</CardDescription>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="newPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>New Password</FormLabel>
                  <div className="relative group">
                    <FormControl>
                      <Input type="password" placeholder="••••••••" {...field} />
                    </FormControl>
                    <div className={cn(
                      'live-password-checklist-tooltip',
                      anyPasswordRuleFailing && 'opacity-100 pointer-events-auto'
                    )}>
                      <ul className="space-y-1 text-xs">
                        {rules.map(rule => {
                          const passed = rule.test(newPassword)
                          return (
                            <li key={rule.label} className={cn('flex items-center gap-1.5', passed ? 'text-green-600' : 'text-muted-foreground')}>
                              {passed ? <CheckCircle className="h-3.5 w-3.5 shrink-0" /> : <XCircle className="h-3.5 w-3.5 shrink-0" />}
                              {rule.label}
                            </li>
                          )
                        })}
                      </ul>
                    </div>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="confirmPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Confirm Password</FormLabel>
                  <div className="relative group">
                    <FormControl>
                      <Input type="password" placeholder="••••••••" {...field} />
                    </FormControl>
                    <div className={cn(
                      'live-password-checklist-tooltip',
                      passwordsNotMatching && 'opacity-100 pointer-events-auto'
                    )}>
                      <ul className="text-xs">
                        <li className={cn('flex items-center gap-1.5', confirmPassword && newPassword === confirmPassword ? 'text-green-600' : 'text-muted-foreground')}>
                          {confirmPassword && newPassword === confirmPassword
                            ? <CheckCircle className="h-3.5 w-3.5 shrink-0" />
                            : <XCircle className="h-3.5 w-3.5 shrink-0" />}
                          Passwords match
                        </li>
                      </ul>
                    </div>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Changing password...' : 'Change Password'}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}
