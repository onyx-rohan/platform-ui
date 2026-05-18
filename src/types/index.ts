export type UserType = 'CONSUMER' | 'BUSINESS' | 'ADMIN' | 'SUPER'
export type PaymentMethod = 'CARD' | 'CASH'
export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'DEACTIVATED' | 'PENDING_DEACTIVATION'
export type SubscriptionStatus = 'PENDING_PAYMENT' | 'GRACE_PERIOD' | 'DEACTIVATED' | 'ACTIVE'
export type DueCycle = 'WEEKLY' | 'BI_WEEKLY' | 'MONTHLY' | 'SEMI_MONTHLY' | 'BI_MONTHLY' | 'QUARTERLY' | 'SEMI_YEARLY' | 'YEARLY'
export type OfferingTier = 'SIMPLE' | 'PRO' | 'ENTERPRISE' | 'LIMITED' | 'NONE'

export interface BusinessType {
  id: number
  type: string
  createdAt: string
  updatedAt: string
}

export interface User {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string
  country: string
  role: UserType
  status: AccountStatus
  deleted: boolean
  business?: Business
  createdAt: string
  updatedAt: string
}

export interface Business {
  id: number
  name: string
  phone: string
  country: string
  vatNumber: string
  type: BusinessType
  subscription?: Subscription
  owner: User
  customerToken: string
  cardToken: string
  status: AccountStatus
  deleted: boolean
  createdAt: string
  updatedAt: string
}

export interface Subscription {
  id: number
  totalPrice: number
  gracePeriod: string
  dueCycle: DueCycle
  status: SubscriptionStatus
  nextDueDate: string
  deleted: boolean
  createdAt: string
  updatedAt: string
}

export interface Product {
  id: number
  name: string
  description: string
  deleted: boolean
  createdAt: string
  updatedAt: string
}

export interface ProductOffering {
  id: number
  tier: OfferingTier
  description: string
  price: number
  limit: number
  resetCycle: DueCycle
  paymentMethods: PaymentMethod[]
  product: Product
  deleted: boolean
  createdAt: string
  updatedAt: string
}