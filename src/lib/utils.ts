import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'
import type { DueCycle, UserType } from '@/types'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function isAdminRole(role?: UserType): boolean {
  return role === 'ADMIN' || role === 'SUPER'
}

export function formatCycle(cycle: DueCycle): string {
  const map: Record<DueCycle, string> = {
    WEEKLY: 'Weekly',
    BI_WEEKLY: 'Bi-Weekly',
    MONTHLY: 'Monthly',
    SEMI_MONTHLY: 'Semi-Monthly',
    BI_MONTHLY: 'Bi-Monthly',
    QUARTERLY: 'Quarterly',
    SEMI_YEARLY: 'Semi-Yearly',
    YEARLY: 'Yearly',
  }
  return map[cycle] ?? cycle
}