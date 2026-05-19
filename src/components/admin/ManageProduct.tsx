import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useAdminLayout } from '@/app/layouts/AdminLayout'
import { useProductMutations } from '@/hooks/useProductMutations'
import type { Product } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().min(1, 'Description is required'),
})

type FormValues = z.infer<typeof schema>

export default function ManageProduct({ product }: { product? : Product }) {
  const { mode, close } = useAdminLayout()
  const { create, update, softDelete, hardDelete } = useProductMutations()

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: product?.name ?? '',
      description: product?.description ?? '',
    },
  })

  async function onSubmit(values: FormValues) {
    try {
      if (mode === 'edit') {
        await update.mutateAsync({ id: product!.id, payload: values })
        toast.success(`Updated ${values.name}`)
      } else {
        await create.mutateAsync(values)
        toast.success(`Created ${values.name}`)
      }
      close()
    } catch {
      toast.error(mode === 'edit' ? 'Failed to update product' : 'Failed to create product')
    }
  }

  async function handleSoftDelete() {
    try {
      await softDelete.mutateAsync(product!.id)
      toast.success(`Soft-deleted ${product!.name}`)
      close()
    } catch {
      toast.error('Failed to soft-delete product')
    }
  }

  async function handleHardDelete() {
    try {
      await hardDelete.mutateAsync(product!.id)
      toast.success(`Deleted ${product!.name}`)
      close()
    } catch {
      toast.error('Failed to delete product')
    }
  }

  const title = mode === 'create' ? 'Create Product'
    : mode === 'edit' ? `Edit — ${product!.name}`
    : mode === 'soft-delete' ? `Soft Delete — ${product!.name}`
    : `Hard Delete — ${product!.name}`

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
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input disabled={mode === 'soft-delete' || mode === 'hard-delete'} placeholder="Product name" {...field} />
                  </FormControl>
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
                      placeholder="Product description..."
                      {...field}
                    />
                  </FormControl>
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