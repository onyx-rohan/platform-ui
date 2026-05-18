import { createBrowserRouter, Navigate } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import ProtectedRoute from '@/components/ProtectedRoute'
import AdminRoute from '@/components/AdminRoute'
import { LoginRedirect, SignUpRedirect } from '@/app/auth/AuthRedirect'
import HomePage from '@/app/home/HomePage'
import ProductsPage from '@/app/products/ProductsPage'
import CompanyPage from '@/app/company/CompanyPage'
import BusinessPage from '@/app/business/BusinessPage'
import ProfilePage from '@/app/user/ProfilePage'
import ManageUsersPage from '@/app/admin/ManageUsersPage'
import ManageBusinessesPage from '@/app/admin/ManageBusinessesPage'

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      { path: '/',                  element: <HomePage /> },
      { path: '/products',          element: <ProductsPage /> },
      { path: '/products/:id',      element: <ProductsPage /> },
      { path: '/company',           element: <CompanyPage /> },
      { path: '/login',             element: <LoginRedirect /> },
      { path: '/sign-up',           element: <SignUpRedirect /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: '/profile',               element: <ProfilePage /> },
          { path: '/business/:id',          element: <BusinessPage /> },
          {
            element: <AdminRoute />,
            children: [
              { path: '/admin/users',             element: <ManageUsersPage /> },
              { path: '/admin/users/:id',         element: <ManageUsersPage /> },
              { path: '/admin/businesses',        element: <ManageBusinessesPage /> },
              { path: '/admin/businesses/:id',    element: <ManageBusinessesPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
