import { createBrowserRouter, Navigate } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import ProtectedRoute from '@/components/ProtectedRoute'
import AdminRoute from '@/components/AdminRoute'
import HomePage from '@/features/home/HomePage'
import ProductsPage from '@/features/products/ProductsPage'
import CompanyPage from '@/features/company/CompanyPage'
import LoginPage from '@/features/auth/LoginPage'
import SignUpPage from '@/features/auth/SignUpPage'
import ForgotPasswordPage from '@/features/auth/ForgotPasswordPage'
import OTPValidationPage from '@/features/auth/OTPValidationPage'
import ChangePasswordPage from '@/features/auth/ChangePasswordPage'
import ProfilePage from '@/features/user/ProfilePage'
import BusinessPage from '@/features/business/BusinessPage'
import ManageUsersPage from '@/features/admin/ManageUsersPage'
import ManageBusinessesPage from '@/features/admin/ManageBusinessesPage'
import ManageProductsPage from '@/features/admin/ManageProductsPage'

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      { path: '/',                  element: <HomePage /> },
      { path: '/products',          element: <ProductsPage /> },
      { path: '/products/:id',      element: <ProductsPage /> },
      { path: '/company',           element: <CompanyPage /> },
      { path: '/login',             element: <LoginPage /> },
      { path: '/sign-up',           element: <SignUpPage /> },
      { path: '/forgot-password',   element: <ForgotPasswordPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: '/otp-validation',  element: <OTPValidationPage /> },
          { path: '/change-password', element: <ChangePasswordPage /> },
          { path: '/profile',         element: <ProfilePage /> },
          { path: '/business/:id',    element: <BusinessPage /> },
          {
            element: <AdminRoute />,
            children: [
              { path: '/admin/users',             element: <ManageUsersPage /> },
              { path: '/admin/users/:id',         element: <ManageUsersPage /> },
              { path: '/admin/businesses',        element: <ManageBusinessesPage /> },
              { path: '/admin/businesses/:id',    element: <ManageBusinessesPage /> },
              { path: '/admin/products',          element: <ManageProductsPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])