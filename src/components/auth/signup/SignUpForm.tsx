import { useMemo, useState } from 'react'
import { signUp } from 'aws-amplify/auth'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { CheckCircle, XCircle } from 'lucide-react'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { cn } from '@/lib/utils'
import { createUser } from '@/api/users'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

const ISO_CARIBBEAN_CODES = [
  'AG','AI','AW','BB','BL','BQ','BS','CU','CW','DM','DO','GD','GP','GY','HT',
  'JM','KN','KY','LC','MF','MQ','MS','PR','SX','TC','TT','VC','VG','VI',
]

const passwordRules = [
  { label: 'Minimum 8 characters',          test: (v: string) => v.length >= 8 },
  { label: 'Maximum 32 characters',          test: (v: string) => v.length <= 32 && v.length > 0 },
  { label: 'At least one uppercase letter',  test: (v: string) => /[A-Z]/.test(v) },
  { label: 'At least one lowercase letter',  test: (v: string) => /[a-z]/.test(v) },
  { label: 'At least one number',            test: (v: string) => /[0-9]/.test(v) },
  { label: 'At least one special character', test: (v: string) => /[#?!@$%^&*-]/.test(v) },
]

const schema = z.object({
  firstName: z.string().min(3, 'Min 3 characters').max(24, 'Max 24 characters'),
  lastName: z.string().min(3, 'Min 3 characters').max(24, 'Max 24 characters'),
  email: z.email({ message: 'Please enter a valid email address' }),
  password: z.string()
    .min(8, 'Min 8 characters')
    .max(32, 'Max 32 characters')
    .regex(/[A-Z]/, 'Must contain an uppercase letter')
    .regex(/[a-z]/, 'Must contain a lowercase letter')
    .regex(/[0-9]/, 'Must contain a number')
    .regex(/[#?!@$%^&*-]/, 'Must contain a special character'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
  country: z.string().min(1, 'Please select a country'),
  phone: z.string().min(1, 'Phone is required'),
  businessName: z.string().optional(),
  businessPhone: z.string().optional(),
  businessCountry: z.string().optional(),
  vatNumber: z.string().optional(),
}).refine(data => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
})

type FormValues = z.infer<typeof schema>

export default function SignUpForm() {
  const { openLogin, openOTP } = useAuthLayout()
  const [isLoading, setIsLoading] = useState(false)
  const [openSection, setOpenSection] = useState<string>('user-info')

  const countries = useMemo(() => {
    const fmt = new Intl.DisplayNames(['en'], { type: 'region' })
    return ISO_CARIBBEAN_CODES
      .map(code => ({ code, name: fmt.of(code) ?? code }))
      .sort((a, b) => a.name.localeCompare(b.name))
  }, [])

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: '', lastName: '', email: '', password: '',
      confirmPassword: '', country: '', phone: '',
      businessName: '', businessPhone: '', businessCountry: '', vatNumber: '',
    },
  })

  const password = form.watch('password')
  const confirmPassword = form.watch('confirmPassword')

  const anyPasswordRuleFailing = password.length > 0 && passwordRules.some(rule => !rule.test(password))
  const passwordsNotMatching = confirmPassword.length > 0 && password !== confirmPassword

  async function onSubmit(values: FormValues) {
    setIsLoading(true)
    try {
      await signUp({
        username: values.email,
        password: values.password,
        options: {
          userAttributes: {
            email: values.email,
            given_name: values.firstName,
            family_name: values.lastName,
          },
        },
      })

      await createUser({
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        phone: values.phone,
        country: values.country,
        userType: 'CONSUMER',
      })

      if (values.businessName) {
        sessionStorage.setItem('pendingBusiness', JSON.stringify({
          name: values.businessName,
          phone: values.businessPhone,
          country: values.businessCountry,
          vatNumber: values.vatNumber,
        }))
      }

      openOTP(values.email, 'SIGNUP')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Registration failed'
      toast.error('Sign up failed', { description: message })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card className="w-full max-w-2xl mx-auto">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl">Creating New User</CardTitle>
        <CardDescription>
          Fill in your details below to create your Onyx Platform account.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="auth-form">
            <Accordion type="single" collapsible value={openSection} onValueChange={setOpenSection}>
              <AccordionItem value="user-info">
                <AccordionTrigger>User Information</AccordionTrigger>
                <AccordionContent>
                  <div className="space-y-4 pt-2">
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <FormField
                        control={form.control}
                        name="firstName"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>First Name</FormLabel>
                            <FormControl><Input placeholder="John" {...field} /></FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="lastName"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Last Name</FormLabel>
                            <FormControl><Input placeholder="Doe" {...field} /></FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>

                    <FormField
                      control={form.control}
                      name="email"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Email</FormLabel>
                          <FormControl><Input type="email" placeholder="you@example.com" {...field} /></FormControl>
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
                          <div className="relative group">
                            <FormControl>
                              <Input type="password" placeholder="••••••••" {...field} />
                            </FormControl>
                            <div className={cn('live-password-checklist-tooltip', anyPasswordRuleFailing && 'opacity-100 pointer-events-auto')}>
                              <ul className="space-y-1 text-xs">
                                {passwordRules.map(rule => {
                                  const passed = rule.test(password)
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
                            <div className={cn('live-password-checklist-tooltip', passwordsNotMatching && 'opacity-100 pointer-events-auto')}>
                              <ul className="text-xs">
                                <li className={cn('flex items-center gap-1.5', confirmPassword && password === confirmPassword ? 'text-green-600' : 'text-muted-foreground')}>
                                  {confirmPassword && password === confirmPassword
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

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <FormField
                        control={form.control}
                        name="country"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Country</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger><SelectValue placeholder="Select country" /></SelectTrigger>
                              </FormControl>
                              <SelectContent>
                                {countries.map(c => <SelectItem key={c.code} value={c.name}>{c.name}</SelectItem>)}
                              </SelectContent>
                            </Select>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="phone"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Phone</FormLabel>
                            <FormControl><Input type="tel" placeholder="+1 555 000 0000" {...field} /></FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>

                    <div className="flex justify-end">
                      <Button type="button" variant="outline" onClick={() => setOpenSection('card-info')}>Next</Button>
                    </div>
                  </div>
                </AccordionContent>
              </AccordionItem>

              <AccordionItem value="card-info">
                <AccordionTrigger>Card Information</AccordionTrigger>
                <AccordionContent>
                  <div className="space-y-4 pt-2">
                    <p className="text-sm text-muted-foreground">Card integration coming soon. You can skip this step.</p>
                    <div className="flex justify-end">
                      <Button type="button" variant="outline" onClick={() => setOpenSection('business-info')}>Next</Button>
                    </div>
                  </div>
                </AccordionContent>
              </AccordionItem>

              <AccordionItem value="business-info">
                <AccordionTrigger>Business Information</AccordionTrigger>
                <AccordionContent>
                  <div className="space-y-4 pt-2">
                    <p className="text-sm text-muted-foreground">Optional — fill in if you own a business.</p>

                    <FormField
                      control={form.control}
                      name="businessName"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Business Name</FormLabel>
                          <FormControl><Input placeholder="Acme Corp" {...field} /></FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <FormField
                        control={form.control}
                        name="businessCountry"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Country</FormLabel>
                            <Select onValueChange={field.onChange} value={field.value}>
                              <FormControl>
                                <SelectTrigger><SelectValue placeholder="Select country" /></SelectTrigger>
                              </FormControl>
                              <SelectContent>
                                {countries.map(c => <SelectItem key={c.code} value={c.name}>{c.name}</SelectItem>)}
                              </SelectContent>
                            </Select>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="businessPhone"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Phone</FormLabel>
                            <FormControl><Input type="tel" placeholder="+1 555 000 0000" {...field} /></FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>

                    <FormField
                      control={form.control}
                      name="vatNumber"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>VAT Number</FormLabel>
                          <FormControl><Input placeholder="VAT-123456789" {...field} /></FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>
                </AccordionContent>
              </AccordionItem>
            </Accordion>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Creating account...' : 'Sign Up'}
            </Button>
          </form>
        </Form>
      </CardContent>

      <CardFooter className="auth-card-footer">
        Already have an account?{' '}
        <button type="button" onClick={openLogin} className="auth-link">Login</button>
      </CardFooter>
    </Card>
  )
}