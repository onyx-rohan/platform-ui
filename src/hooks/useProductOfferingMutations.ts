import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  createProductOffering,
  updateProductOffering,
  hardDeleteProductOffering,
  type ProductOfferingRequest,
} from '@/api/products'

export function useOfferingMutations(productId: number) {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['product', productId, 'offerings'] })

  const create = useMutation({
    mutationFn: (payload: ProductOfferingRequest) => createProductOffering(productId, payload),
    onSuccess: invalidate,
  })

  const update = useMutation({
    mutationFn: ({ offeringId, payload }: { offeringId: number; payload: ProductOfferingRequest }) =>
      updateProductOffering(productId, offeringId, payload),
    onSuccess: invalidate,
  })

  const hardDelete = useMutation({
    mutationFn: (offeringId: number) => hardDeleteProductOffering(productId, offeringId),
    onSuccess: invalidate,
  })

  return { create, update, hardDelete }
}