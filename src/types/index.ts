export interface User {
  id: number
  cognitoId: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  role: 'admin' | 'business' | 'user'
  active: boolean
}

export interface Business {
  id: number
  name: string
  phone?: string
  vatNumber?: string
  country: string
  type: string
  active: boolean
  subscriptionStatus: 'active' | 'grace_period' | 'pending_payment' | 'inactive'
  paymentCycle: 'monthly' | 'annual'
  nextPaymentDue?: string
}

export interface Product {
  id: number
  name: string
  description?: string
  active: boolean
}

export interface ProductOffering {
  id: number
  productId: number
  name: string
  price: number
  usageLimit?: number
}

export interface Subscription {
  id: number
  businessId: number
  offeringId: number
  status: 'active' | 'grace_period' | 'pending_payment' | 'inactive'
  startDate: string
  nextPaymentDue?: string
}