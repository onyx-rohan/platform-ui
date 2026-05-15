import type { Product, ProductOffering } from '@/types'
import apiClient from './client'

export async function getProducts(): Promise<Product[]> {
  const { data } = await apiClient.get<Product[]>('/api/product')
  return data
}

export async function getProduct(id: number): Promise<Product> {
  const { data } = await apiClient.get<Product>(`/api/product/${id}`)
  return data
}

export async function getProductOfferings(id: number): Promise<ProductOffering[]> {
  const { data } = await apiClient.get<ProductOffering[]>(`/api/product/${id}/offering`)
  return data
}