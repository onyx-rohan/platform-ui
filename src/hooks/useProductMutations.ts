import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createProduct, updateProduct, hardDeleteProduct, type ProductRequest } from '@/api/products'

export function useProductMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['products'] })

  const create = useMutation({
    mutationFn: (payload: ProductRequest) => createProduct(payload),
    onSuccess: invalidate,
  })

  const update = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: ProductRequest }) => updateProduct(id, payload),
    onSuccess: invalidate,
  })

  const hardDelete = useMutation({
    mutationFn: (id: number) => hardDeleteProduct(id),
    onSuccess: invalidate,
  })

  return { create, update, hardDelete }
}