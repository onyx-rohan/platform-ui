import { Link, Outlet } from 'react-router-dom'
import { ChevronDown, LogOut, User as UserIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { NavDropdown } from '@/components/NavDropdown'
import { useAuth } from '@/hooks/useAuth'
import { useCurrentUser } from '@/hooks/useCurrentUser'
import { useProducts } from '@/hooks/useProducts'
import AuthLayout, { useAuthLayout } from '@/app/layouts/AuthLayout'

export default function MainLayout() {
  return (
    <AuthLayout>
      <MainLayoutInner />
    </AuthLayout>
  )
}

function MainLayoutInner() {
  const { isAuthenticated, signOut } = useAuth()
  const { data: currentUser } = useCurrentUser()
  const { data: products } = useProducts()
  const { openLoginModal, openSignUpModal } = useAuthLayout()

  const isAdmin = currentUser?.role === 'ADMIN' || currentUser?.role === 'SUPER'

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
                <Button variant="ghost" size="sm" className="gap-1">
                  Products <ChevronDown className="h-3 w-3" />
                </Button>
              }
            >
              {products?.map(product => (
                <DropdownMenuItem key={product.id} asChild>
                  <Link to={`/products/${product.id}`}>{product.name}</Link>
                </DropdownMenuItem>
              ))}
            </NavDropdown>

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
                <DropdownMenuItem asChild>
                  <Link to="/admin/users">Manage Users</Link>
                </DropdownMenuItem>
                <DropdownMenuItem asChild>
                  <Link to="/admin/businesses">Manage Businesses</Link>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild>
                  <Link to="/admin/products">Manage Products</Link>
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
                <Button variant="ghost" size="sm" onClick={openLoginModal}>
                  Login
                </Button>
                <Button size="sm" onClick={openSignUpModal}>
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
