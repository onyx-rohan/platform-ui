import { useQuery } from '@tanstack/react-query'
import { getBusinessProductUsage } from '@/api/business'

export function useBusinessProductUsage(businessId: number) {
  return useQuery({
    queryKey: ['business', businessId, 'product-usage'],
    queryFn: () => getBusinessProductUsage(businessId),
    enabled: !!businessId,
  })
}