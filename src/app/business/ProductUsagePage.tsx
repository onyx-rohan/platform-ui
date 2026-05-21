import { Link, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useBusinessProductUsage } from '@/hooks/useBusinessProductUsage'
import { formatCycle } from '@/lib/utils'
import type { OfferingTier } from '@/types'

function tierVariant(tier: OfferingTier): 'default' | 'secondary' | 'outline' {
  if (tier === 'PRO' || tier === 'ENTERPRISE') return 'default'
  if (tier === 'SIMPLE') return 'secondary'
  return 'outline'
}

export default function ProductUsagePage() {
  const { id } = useParams()
  const businessId = Number(id)
  const { data: productUsages, isLoading } = useBusinessProductUsage(businessId)

  if (isLoading) {
    return (
      <div className="page-container">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  return (
    <div className="page-container">
      <div className="flex items-center mb-6">
        <Button variant="ghost" size="sm" asChild className="-ml-2">
          <Link to={`/business/${businessId}`}>
            <ArrowLeft className="mr-1.5 h-4 w-4" /> Back
          </Link>
        </Button>
      </div>

      <h1 className="page-title mb-8">Product Usage</h1>

      {!productUsages || productUsages.length === 0 ? (
        <p className="text-muted-foreground">No products in use.</p>
      ) : (
        <div className="product-grid">
          {productUsages.map(usage => {
            const offering = usage.productOffering
            const product = offering.product
            return (
              <Card key={usage.id} className="flex flex-col">
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <CardTitle>{product.name}</CardTitle>
                    <Badge variant={tierVariant(offering.tier)}>{offering.tier}</Badge>
                  </div>
                  <CardDescription>{offering.description}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-2 text-sm">
                  <div className="detail-row">
                    <span className="text-muted-foreground">Usage</span>
                    <span className="font-medium">{usage.usage} / {usage.limit}</span>
                  </div>
                  <div className="detail-row">
                    <span className="text-muted-foreground">Resets</span>
                    <span className="font-medium">{formatCycle(usage.resetCycle)}</span>
                  </div>
                  {product.description && (
                    <p className="text-muted-foreground pt-1">{product.description}</p>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}