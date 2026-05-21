import type { ProductUsage } from '@/types'
import apiClient from './client'

export async function getBusinessProductUsage(businessId: number): Promise<ProductUsage[]> {
  const { data } = await apiClient.get<ProductUsage[]>(`/api/business/${businessId}/product-usage`)
  return data
}