import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'
import type { DueCycle, UserType } from '@/types'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function isAdminRole(role?: UserType): boolean {
  return role === 'ADMIN' || role === 'SUPER'
}

export const passwordRules = [
  { label: 'Minimum 8 characters',          test: (v: string) => v.length >= 8 },
  { label: 'Maximum 32 characters',          test: (v: string) => v.length <= 32 && v.length > 0 },
  { label: 'At least one uppercase letter',  test: (v: string) => /[A-Z]/.test(v) },
  { label: 'At least one lowercase letter',  test: (v: string) => /[a-z]/.test(v) },
  { label: 'At least one number',            test: (v: string) => /[0-9]/.test(v) },
  { label: 'At least one special character', test: (v: string) => /[#?!@$%^&*-]/.test(v) },
]

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