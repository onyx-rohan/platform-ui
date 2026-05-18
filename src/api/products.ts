import type { DueCycle, OfferingTier, PaymentMethod, Product, ProductOffering } from '@/types'
import apiClient from './client'

export interface ProductRequest {
  name: string
  description: string
  deleted?: boolean
}

export interface ProductOfferingRequest {
  tier: OfferingTier
  description: string
  resetCycle: DueCycle
  limit: number
  price: number
  paymentMethods: PaymentMethod[]
  deleted?: boolean
}

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

export async function createProductOffering(productId: number, payload: ProductOfferingRequest): Promise<ProductOffering> {
  const { data } = await apiClient.post<ProductOffering>(`/api/product/${productId}/offering`, payload)
  return data
}

export async function updateProductOffering(productId: number, offeringId: number, payload: ProductOfferingRequest): Promise<ProductOffering> {
  const { data } = await apiClient.put<ProductOffering>(`/api/product/${productId}/offering/${offeringId}`, payload)
  return data
}

export async function hardDeleteProductOffering(productId: number, offeringId: number): Promise<void> {
  await apiClient.delete(`/api/product/${productId}/offering/${offeringId}`)
}

export async function createProduct(payload: ProductRequest): Promise<Product> {
  const { data } = await apiClient.post<Product>('/api/product', payload)
  return data
}

export async function updateProduct(id: number, payload: ProductRequest): Promise<Product> {
  const { data } = await apiClient.put<Product>(`/api/product/${id}`, payload)
  return data
}

export async function hardDeleteProduct(id: number): Promise<void> {
  await apiClient.delete(`/api/product/${id}`)
}