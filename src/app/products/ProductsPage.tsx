import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, CreditCard, Banknote, Plus, Pencil, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { useProducts } from '@/hooks/useProducts'
import { useProduct } from '@/hooks/useProduct'
import { useProductOfferings } from '@/hooks/useProductOfferings'
import { useCurrentUser } from '@/hooks/useCurrentUser'
import { useAdminLayout } from '@/app/layouts/AdminLayout'
import { formatCycle, isAdminRole } from '@/lib/utils'
import type { OfferingTier } from '@/types'

function tierVariant(tier: OfferingTier): 'default' | 'secondary' | 'outline' {
  if (tier === 'PRO' || tier === 'ENTERPRISE') return 'default'
  if (tier === 'SIMPLE') return 'secondary'
  return 'outline'
}

function ProductList() {
  const { data: products, isLoading } = useProducts()
  const { data: currentUser } = useCurrentUser()
  const { manageProduct } = useAdminLayout()

  const isAdmin = isAdminRole(currentUser?.role)

  if (isLoading) {
    return <p className="text-muted-foreground">Loading products...</p>
  }

  return (
    <div className="page-container">
      <div className="flex items-center justify-between mb-8">
        <h1 className="page-title">Products</h1>
        {isAdmin && (
          <Button size="sm" onClick={() => manageProduct('create')}>
            <Plus className="mr-1.5 h-4 w-4" /> New Product
          </Button>
        )}
      </div>
      <div className="product-grid">
        {products?.map(product => (
          <Card key={product.id} className="aspect-5/6 flex flex-col">
            <CardHeader className="flex-1">
              <div className="flex items-start justify-between gap-2">
                <div className="flex-1 min-w-0">
                  <CardTitle>{product.name}</CardTitle>
                  <CardDescription className="mt-1">{product.description}</CardDescription>
                </div>
                {isAdmin && (
                  <div className="flex items-center gap-1 shrink-0">
                    <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => manageProduct('edit', product)}>
                      <Pencil className="h-3.5 w-3.5" />
                    </Button>
                    <Button variant="ghost" size="icon" className="h-7 w-7 text-amber-500 hover:text-amber-600" onClick={() => manageProduct('soft-delete', product)}>
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                    <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive hover:text-destructive" onClick={() => manageProduct('hard-delete', product)}>
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                )}
              </div>
            </CardHeader>
            <div className="px-6 pb-6">
              <Button variant="outline" size="sm" asChild>
                <Link to={`/products/${product.id}`}>View Plans</Link>
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}

function ProductDetail({ id }: { id: number }) {
  const { data: product, isLoading: productLoading } = useProduct(id)
  const { data: productOfferings, isLoading: productOfferingsLoading } = useProductOfferings(id)
  const { data: currentUser } = useCurrentUser()
  const { manageProduct, manageProductOffering } = useAdminLayout()

  const isLoading = productLoading || productOfferingsLoading
  const isAdmin = isAdminRole(currentUser?.role)

  if (isLoading) {
    return <p className="text-muted-foreground">Loading...</p>
  }

  if (!product) {
    return <p className="text-muted-foreground">Product not found.</p>
  }

  return (
    <div className="page-container max-w-4xl">
      <div className="flex items-center mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link to="/products">
            <ArrowLeft className="mr-1.5 h-4 w-4" /> Back to Products
          </Link>
        </Button>
      </div>

      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="page-title">{product.name}</h1>
          <p className="mt-2 text-muted-foreground">{product.description}</p>
        </div>
        {isAdmin && (
          <div className="flex items-center gap-1 shrink-0 mt-1">
            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => manageProduct('edit', product)}>
              <Pencil className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" className="h-7 w-7 text-amber-500 hover:text-amber-600" onClick={() => manageProduct('soft-delete', product)}>
              <Trash2 className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive hover:text-destructive" onClick={() => manageProduct('hard-delete', product)}>
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        )}
      </div>

      <Separator className="my-8" />

      <div className="flex items-center justify-between mb-6">
        <h2 className="section-title">Available Plans</h2>
        {isAdmin && (
          <Button size="sm" onClick={() => manageProductOffering('create')}>
            <Plus className="mr-1.5 h-4 w-4" /> Add Offering
          </Button>
        )}
      </div>

      {!productOfferings || productOfferings.length === 0 ? (
        <p className="text-muted-foreground">No plans available for this product.</p>
      ) : (
        <div className="product-grid">
          {productOfferings.map(productOffering => (
            <Card key={productOffering.id} className="aspect-5/6 flex flex-col">
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <CardTitle className="text-lg">{productOffering.tier}</CardTitle>
                      <Badge variant={tierVariant(productOffering.tier)}>{productOffering.tier}</Badge>
                    </div>
                    <CardDescription className="mt-1">{productOffering.description}</CardDescription>
                  </div>
                  {isAdmin && (
                    <div className="flex items-center gap-1 shrink-0">
                      <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => manageProductOffering('edit', productOffering)}>
                        <Pencil className="h-3.5 w-3.5" />
                      </Button>
                      <Button variant="ghost" size="icon" className="h-7 w-7 text-amber-500 hover:text-amber-600" onClick={() => manageProductOffering('soft-delete', productOffering)}>
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                      <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive hover:text-destructive" onClick={() => manageProductOffering('hard-delete', productOffering)}>
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    </div>
                  )}
                </div>
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                <div className="detail-row">
                  <span className="text-muted-foreground">Price</span>
                  <span className="font-medium">${productOffering.price.toFixed(2)}</span>
                </div>
                <div className="detail-row">
                  <span className="text-muted-foreground">Billing Cycle</span>
                  <span className="font-medium">{formatCycle(productOffering.resetCycle)}</span>
                </div>
                {productOffering.limit > 0 && (
                  <div className="detail-row">
                    <span className="text-muted-foreground">Limit</span>
                    <span className="font-medium">{productOffering.limit}</span>
                  </div>
                )}
                {productOffering.paymentMethods?.length > 0 && (
                  <div className="detail-row">
                    <span className="text-muted-foreground">Payment</span>
                    <div className="flex items-center gap-1.5">
                      {productOffering.paymentMethods.map(method => (
                        method === 'CARD'
                          ? <CreditCard key={method} className="h-4 w-4" />
                          : <Banknote key={method} className="h-4 w-4" />
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

export default function ProductsPage() {
  const { id } = useParams()

  if (id) {
    return <ProductDetail id={Number(id)} />
  }

  return <ProductList />
}