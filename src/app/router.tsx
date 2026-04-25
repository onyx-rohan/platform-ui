import { createBrowserRouter } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [],
  },
])