import { createContext, useContext, useState } from 'react'
import ManageProduct from '@/components/admin/ManageProduct'
import ManageProductOffering from '@/components/admin/ManageProductOffering'
import type { ReactNode } from 'react'
import type { Product, ProductOffering } from '@/types'

export type AdminMode = 'create' | 'edit' | 'delete'

type ActiveForm =
  | { type: 'product'; data?: Product }
  | { type: 'product-offering'; data?: ProductOffering }
  | null

interface AdminContext {
  manageProduct: (mode: AdminMode, product?: Product) => void
  manageProductOffering: (mode: AdminMode, product_offering?: ProductOffering) => void
  mode: AdminMode
  close: () => void
}

const AdminLayoutContext = createContext<AdminContext | null>(null)

export function useAdminLayout() {
  const ctx = useContext(AdminLayoutContext)
  if (!ctx) throw new Error('useAdminLayout must be used within AdminLayout')
  return ctx
}

export default function AdminLayout({ children }: { children: ReactNode }) {
  const [activeForm, setActiveForm] = useState<ActiveForm>(null)
  const [mode, setMode] = useState<AdminMode>('create')

  function manageProduct(mode: AdminMode, product?: Product) {
    setActiveForm({ type: 'product', data: product })
    setMode(mode)
  }

  function manageProductOffering(mode: AdminMode, product_offering?: ProductOffering) {
    setActiveForm({ type: 'product-offering', data: product_offering })
    setMode(mode)
  }

  function close() {
    setActiveForm(null)
  }

  return (
    <AdminLayoutContext.Provider value={{ mode, manageProduct, manageProductOffering, close }}>
      {children}
      {activeForm && (
        <div className="admin-modal-overlay">
          <div className="admin-modal-backdrop" onClick={close} />
          <div className="admin-modal-content">
            {activeForm.type === 'product' && <ManageProduct product={activeForm.data} />}
            {activeForm.type === 'product-offering' && <ManageProductOffering product_offering={activeForm.data} />}
          </div>
        </div>
      )}
    </AdminLayoutContext.Provider>
  )
}