import * as React from "react";
import { useState } from 'react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

interface NavDropdownProps {
  trigger: React.ReactNode
  children: React.ReactNode
  align?: 'start' | 'center' | 'end'
}

export function NavDropdown({ trigger, children, align = 'start' }: NavDropdownProps) {
  const [isOpen, setIsOpen] = useState(false)

  return (
    <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
      <DropdownMenuTrigger
        asChild
        data-nav-trigger
        onMouseEnter={() => setIsOpen(true)}
      >
        {trigger}
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align={align}
        onCloseAutoFocus={e => e.preventDefault()}
        onEscapeKeyDown={() => setIsOpen(false)}
      >
        {children}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
