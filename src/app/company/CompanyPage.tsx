import { Mail, MapPin, Phone } from 'lucide-react'
import { Separator } from '@/components/ui/separator'

export default function CompanyPage() {
  return (
    <div className="page-container max-w-3xl">
      <h1 className="page-title">About Onyx Softworks</h1>
      <p className="mt-2 text-muted-foreground">Building modern solutions for Caribbean businesses.</p>

      <Separator className="my-8" />

      <section className="space-y-4">
        <h2 className="section-title">Our Mission</h2>
        <p className="text-muted-foreground leading-relaxed">
          Onyx Softworks exists to simplify how Caribbean businesses manage their operations.
          From subscriptions and payments to customer and product management, we provide a
          unified platform that removes complexity so you can focus on growth.
        </p>
      </section>

      <Separator className="my-8" />

      <section className="space-y-4">
        <h2 className="section-title">What We Do</h2>
        <p className="text-muted-foreground leading-relaxed">
          The Onyx Platform connects businesses, consumers, and services under one roof.
          Whether you're a sole trader or a growing company, our tools are designed to
          scale with you — handling billing cycles, account management, and service
          subscriptions with minimal overhead.
        </p>
      </section>

      <Separator className="my-8" />

      <section className="space-y-4">
        <h2 className="section-title">Contact</h2>
        <ul className="space-y-3 text-muted-foreground">
          <li className="flex items-center gap-2">
            <MapPin className="h-4 w-4 shrink-0" />
            Caribbean Region
          </li>
          <li className="flex items-center gap-2">
            <Phone className="h-4 w-4 shrink-0" />
            +1 (000) 000-0000
          </li>
          <li className="flex items-center gap-2">
            <Mail className="h-4 w-4 shrink-0" />
            contact@onyxsoftworks.com
          </li>
        </ul>
      </section>
    </div>
  )
}