import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useAdminLayout } from '@/app/layouts/AdminLayout'
import { useOfferingMutations } from '@/hooks/useProductOfferingMutations'
import { formatCycle } from '@/lib/utils'
import type { DueCycle, OfferingTier, PaymentMethod, ProductOffering } from '@/types'

const OFFERING_TIERS: OfferingTier[] = ['SIMPLE', 'PRO', 'ENTERPRISE', 'LIMITED', 'NONE']
const DUE_CYCLES: DueCycle[] = ['WEEKLY', 'BI_WEEKLY', 'MONTHLY', 'SEMI_MONTHLY', 'BI_MONTHLY', 'QUARTERLY', 'SEMI_YEARLY', 'YEARLY']
const PAYMENT_METHODS: PaymentMethod[] = ['CARD', 'CASH', 'ON_PAYOUT']

const schema = z.object({
  tier: z.enum(['SIMPLE', 'PRO', 'ENTERPRISE', 'LIMITED', 'NONE'] as const),
  description: z.string().min(1, 'Description is required'),
  resetCycle: z.enum(['WEEKLY', 'BI_WEEKLY', 'MONTHLY', 'SEMI_MONTHLY', 'BI_MONTHLY', 'QUARTERLY', 'SEMI_YEARLY', 'YEARLY'] as const),
  limit: z.number().min(0, 'Limit must be 0 or greater'),
  price: z.number().min(0, 'Price must be 0 or greater'),
  paymentMethods: z.array(z.enum(['CARD', 'CASH', 'ON_PAYOUT'] as const)).min(1, 'At least one payment method is required'),
})

type FormValues = z.infer<typeof schema>


export default function ManageProductOffering({ product_offering }: { product_offering?: ProductOffering }) {
  const { mode, close } = useAdminLayout()
  const { create, update, softDelete, hardDelete } = useOfferingMutations(product_offering!.product!.id)

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: product_offering
      ? {
          tier: product_offering.tier,
          description: product_offering.description,
          resetCycle: product_offering.resetCycle,
          limit: product_offering.limit,
          price: product_offering.price,
          paymentMethods: product_offering.paymentMethods ?? [],
        }
      : { tier: 'SIMPLE', description: '', resetCycle: 'MONTHLY', limit: 0, price: 0, paymentMethods: [] },
  })

  async function onSubmit(values: FormValues) {
    try {
      if (mode === 'edit') {
        await update.mutateAsync({ offeringId: product_offering!.id, payload: values })
        toast.success(`Updated ${values.tier} offering`)
      } else {
        await create.mutateAsync(values)
        toast.success(`Created ${values.tier} offering`)
      }
      close()
    } catch {
      toast.error(mode === 'edit' ? 'Failed to update offering' : 'Failed to create offering')
    }
  }

  async function handleSoftDelete() {
    try {
      await softDelete.mutateAsync(product_offering!.id)
      toast.success(`Soft-deleted ${product_offering!.tier} offering`)
      close()
    } catch {
      toast.error('Failed to soft-delete offering')
    }
  }

  async function handleHardDelete() {
    try {
      await hardDelete.mutateAsync(product_offering!.id)
      toast.success(`Deleted ${product_offering!.tier} offering`)
      close()
    } catch {
      toast.error('Failed to delete offering')
    }
  }

  function toggleMethod(method: PaymentMethod, current: PaymentMethod[]): PaymentMethod[] {
    return current.includes(method) ? current.filter(m => m !== method) : [...current, method]
  }

  const title = mode === 'create'
    ? `Add Offering — ${product_offering!.product!.name}`
    : mode === 'edit' ? `Edit Offering — ${product_offering!.tier}`
    : mode === 'soft-delete' ? `Soft Delete Offering — ${product_offering!.tier}`
    : `Hard Delete Offering — ${product_offering!.tier}`

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={mode !== 'soft-delete' && mode !== 'hard-delete' ? form.handleSubmit(onSubmit) : e => e.preventDefault()} className="space-y-4">
            <FormField
              control={form.control}
              name="tier"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Tier</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value} disabled={mode === 'soft-delete' || mode === 'hard-delete'}>
                    <FormControl>
                      <SelectTrigger><SelectValue placeholder="Select tier" /></SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {OFFERING_TIERS.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description</FormLabel>
                  <FormControl>
                    <textarea
                      disabled={mode === 'soft-delete' || mode === 'hard-delete'}
                      className="flex min-h-20 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      placeholder="Offering description..."
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="resetCycle"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Billing Cycle</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value} disabled={mode === 'soft-delete' || mode === 'hard-delete'}>
                    <FormControl>
                      <SelectTrigger><SelectValue placeholder="Select billing cycle" /></SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {DUE_CYCLES.map(c => <SelectItem key={c} value={c}>{formatCycle(c)}</SelectItem>)}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="limit"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Limit</FormLabel>
                    <FormControl>
                      <Input
                        type="number" min={0} step={1}
                        disabled={mode === 'soft-delete' || mode === 'hard-delete'}
                        {...field}
                        onChange={e => field.onChange(Number(e.target.value))}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="price"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Price (USD)</FormLabel>
                    <FormControl>
                      <Input
                        type="number" min={0} step={0.01}
                        disabled={mode === 'soft-delete' || mode === 'hard-delete'}
                        {...field}
                        onChange={e => field.onChange(Number(e.target.value))}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="paymentMethods"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Payment Methods</FormLabel>
                  <div className="flex gap-4">
                    {PAYMENT_METHODS.map(method => (
                      <label
                        key={method}
                        className={`flex items-center gap-2 select-none text-sm ${mode === 'soft-delete' || mode === 'hard-delete' ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'}`}
                      >
                        <input
                          type="checkbox"
                          className="h-4 w-4 rounded border-border accent-primary"
                          disabled={mode === 'soft-delete' || mode === 'hard-delete'}
                          checked={field.value.includes(method)}
                          onChange={() => field.onChange(toggleMethod(method, field.value))}
                        />
                        {method}
                      </label>
                    ))}
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="ghost" onClick={close}>
                Cancel
              </Button>
              {mode === 'soft-delete' ? (
                <Button
                  type="button"
                  className="bg-amber-500 hover:bg-amber-600 text-white"
                  disabled={softDelete.isPending}
                  onClick={handleSoftDelete}
                >
                  {softDelete.isPending ? 'Deleting...' : 'Soft Delete'}
                </Button>
              ) : mode === 'hard-delete' ? (
                <Button
                  type="button"
                  variant="destructive"
                  disabled={hardDelete.isPending}
                  onClick={handleHardDelete}
                >
                  {hardDelete.isPending ? 'Deleting...' : 'Hard Delete'}
                </Button>
              ) : (
                <Button type="submit" disabled={create.isPending || update.isPending}>
                  {create.isPending || update.isPending ? 'Saving...' : mode === 'create' ? 'Create' : 'Update'}
                </Button>
              )}
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}