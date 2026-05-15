import { useQuery } from '@tanstack/react-query'
import { getProductOfferings } from '@/api/products'

export function useProductOfferings(productId: number) {
  return useQuery({
    queryKey: ['product', productId, 'offerings'],
    queryFn: () => getProductOfferings(productId),
    enabled: !!productId,
  })
}