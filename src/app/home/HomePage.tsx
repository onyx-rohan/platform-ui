import { Link } from 'react-router-dom'
import { ArrowRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuth } from '@/hooks/useAuth'
import { useProducts } from '@/hooks/useProducts'
import { useAuthLayout } from '@/app/layouts/AuthLayout'

export default function HomePage() {
  const { isAuthenticated } = useAuth()
  const { openLoginModal, openSignUpModal } = useAuthLayout()
  const { data: products } = useProducts()

  return (
    <div className="flex flex-col">
      <section className="flex flex-col items-center justify-center gap-6 px-4 py-24 text-center">
        <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">
          Onyx Platform
        </h1>
        <p className="max-w-xl text-lg text-muted-foreground">
          A unified platform for managing your business, subscriptions, and services across the Caribbean.
        </p>
        {!isAuthenticated && (
          <div className="flex gap-3">
            <Button size="lg" onClick={openSignUpModal}>
              Get Started
            </Button>
            <Button size="lg" variant="outline" onClick={openLoginModal}>
              Login
            </Button>
          </div>
        )}
      </section>

      {products && products.length > 0 && (
        <section className="container mx-auto px-4 pb-24">
          <h2 className="mb-8 text-2xl font-semibold tracking-tight">Our Products</h2>
          <div className="product-grid">
            {products.map(product => (
              <Card key={product.id} className="flex flex-col">
                <CardHeader className="flex-1">
                  <CardTitle>{product.name}</CardTitle>
                  <CardDescription>{product.description}</CardDescription>
                </CardHeader>
                <div className="px-6 pb-6">
                  <Button variant="outline" size="sm" asChild>
                    <Link to={`/products/${product.id}`}>
                      View Plans <ArrowRight className="ml-1.5 h-3.5 w-3.5" />
                    </Link>
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}