import { Link, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { useProducts } from '@/hooks/useProducts'
import { useProduct } from '@/hooks/useProduct'
import { useProductOfferings } from '@/hooks/useProductOfferings'
import type { DueCycle, OfferingTier } from '@/types'

function formatCycle(cycle: DueCycle): string {
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

function tierVariant(tier: OfferingTier): 'default' | 'secondary' | 'outline' {
  if (tier === 'PRO' || tier === 'ENTERPRISE') return 'default'
  if (tier === 'SIMPLE') return 'secondary'
  return 'outline'
}

function ProductList() {
  const { data: products, isLoading } = useProducts()

  if (isLoading) {
    return <p className="text-muted-foreground">Loading products...</p>
  }

  return (
    <div className="page-container">
      <h1 className="page-title mb-8">Products</h1>
      <div className="product-grid">
        {products?.map(product => (
          <Card key={product.id} className="flex flex-col">
            <CardHeader className="flex-1">
              <CardTitle>{product.name}</CardTitle>
              <CardDescription>{product.description}</CardDescription>
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
  const { data: offerings, isLoading: offeringsLoading } = useProductOfferings(id)

  const isLoading = productLoading || offeringsLoading

  if (isLoading) {
    return <p className="text-muted-foreground">Loading...</p>
  }

  if (!product) {
    return <p className="text-muted-foreground">Product not found.</p>
  }

  return (
    <div className="page-container max-w-4xl">
      <Button variant="ghost" size="sm" asChild className="mb-6 -ml-2">
        <Link to="/products">
          <ArrowLeft className="mr-1.5 h-4 w-4" /> Back to Products
        </Link>
      </Button>

      <h1 className="page-title">{product.name}</h1>
      <p className="mt-2 text-muted-foreground">{product.description}</p>

      <Separator className="my-8" />

      <h2 className="section-title mb-6">Available Plans</h2>

      {!offerings || offerings.length === 0 ? (
        <p className="text-muted-foreground">No plans available for this product.</p>
      ) : (
        <div className="product-grid">
          {offerings.map(offering => (
            <Card key={offering.id}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">{offering.tier}</CardTitle>
                  <Badge variant={tierVariant(offering.tier)}>{offering.tier}</Badge>
                </div>
                <CardDescription>{offering.description}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                <div className="detail-row">
                  <span className="text-muted-foreground">Price</span>
                  <span className="font-medium">${offering.price.toFixed(2)}</span>
                </div>
                <div className="detail-row">
                  <span className="text-muted-foreground">Billing Cycle</span>
                  <span className="font-medium">{formatCycle(offering.resetCycle)}</span>
                </div>
                {offering.limit > 0 && (
                  <div className="detail-row">
                    <span className="text-muted-foreground">Limit</span>
                    <span className="font-medium">{offering.limit}</span>
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