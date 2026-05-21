import { Link, Outlet } from 'react-router-dom'
import { ChevronDown, LogOut, User as UserIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DropdownMenuItem, DropdownMenuSeparator, DropdownMenuSub, DropdownMenuSubTrigger, DropdownMenuSubContent } from '@/components/ui/dropdown-menu'
import { NavDropdown } from '@/components/NavDropdown'
import { useAuth } from '@/hooks/useAuth'
import { useCurrentUser } from '@/hooks/useCurrentUser'
import { useProducts } from '@/hooks/useProducts'
import { isAdminRole } from '@/lib/utils'
import AuthLayout, { useAuthLayout } from '@/app/layouts/AuthLayout'
import AdminLayout from '@/app/layouts/AdminLayout'

export default function MainLayout() {
  return (
    <AuthLayout>
      <AdminLayout>
        <MainLayoutInner />
      </AdminLayout>
    </AuthLayout>
  )
}

function MainLayoutInner() {
  const { isAuthenticated, signOut } = useAuth()
  const { data: currentUser } = useCurrentUser()
  const { data: products } = useProducts()
  const { openLogin, openSignUp } = useAuthLayout()

  const isAdmin = isAdminRole(currentUser?.role)

  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-50 border-b bg-background">
        <div className="container mx-auto flex h-14 items-center gap-6 px-4">

          <Link to="/" className="font-semibold text-lg tracking-tight">
            Onyx
          </Link>

          <nav className="flex items-center gap-1">
            <Button variant="ghost" size="sm" asChild>
              <Link to="/">Home</Link>
            </Button>

            <NavDropdown
              trigger={
                <Button variant="ghost" size="sm" className="px-1.5">
                  Products <ChevronDown className="h-3 w-3" />
                </Button>
              }
            >
              {products?.map(product => (
                <DropdownMenuItem key={product.id} asChild>
                  <Link to={`/products/${product.id}`}>{product.name}</Link>
                </DropdownMenuItem>
              ))}
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link to="/products">View All</Link>
              </DropdownMenuItem>
            </NavDropdown>

            {currentUser?.business && (
              <NavDropdown
                trigger={
                  <Button variant="ghost" size="sm" className="px-1.5">
                    Business <ChevronDown className="h-3 w-3" />
                  </Button>
                }
              >
                <DropdownMenuItem asChild>
                  <Link to={`/business/${currentUser.business.id}/product-usage`}>Product Usage</Link>
                </DropdownMenuItem>
              </NavDropdown>
            )}

            <Button variant="ghost" size="sm" asChild>
              <Link to="/company">Company</Link>
            </Button>
          </nav>

          <div className="ml-auto flex items-center gap-2">
            {isAdmin && (
              <NavDropdown
                align="end"
                trigger={
                  <Button variant="ghost" size="sm" className="gap-1">
                    Admin <ChevronDown className="h-3 w-3" />
                  </Button>
                }
              >
                <DropdownMenuSub>
                  <DropdownMenuSubTrigger title="View and manage products">
                    Products
                  </DropdownMenuSubTrigger>
                  <DropdownMenuSubContent>
                    <DropdownMenuItem asChild>
                      <Link to="/products">All Products</Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Link to="/products" title="Manage pricing tiers and plans — select a product to edit its offerings">
                        Product Offerings
                      </Link>
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuSub>
                <DropdownMenuSub>
                  <DropdownMenuSubTrigger title="View and manage businesses">
                    Businesses
                  </DropdownMenuSubTrigger>
                  <DropdownMenuSubContent>
                    <DropdownMenuItem asChild>
                      <Link to="/admin/businesses">All Businesses</Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Link to="/admin/businesses" title="Manage which products a business is subscribed to — select a business to edit its products">
                        Business Products
                      </Link>
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuSub>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild>
                  <Link to="/admin/users" title="View and manage user accounts">Users</Link>
                </DropdownMenuItem>
              </NavDropdown>
            )}

            {isAuthenticated ? (
              <NavDropdown
                align="end"
                trigger={
                  <Button variant="ghost" size="sm" className="gap-1">
                    <UserIcon className="h-4 w-4" />
                    {currentUser?.firstName ?? 'Account'}
                    <ChevronDown className="h-3 w-3" />
                  </Button>
                }
              >
                <DropdownMenuItem asChild>
                  <Link to="/profile">Profile</Link>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  className="text-destructive focus:text-destructive"
                  onClick={signOut}
                >
                  <LogOut className="h-4 w-4 mr-2" />
                  Logout
                </DropdownMenuItem>
              </NavDropdown>
            ) : (
              <>
                <Button variant="ghost" size="sm" onClick={openLogin}>
                  Login
                </Button>
                <Button size="sm" onClick={openSignUp}>
                  Sign Up
                </Button>
              </>
            )}
          </div>

        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  )
}
