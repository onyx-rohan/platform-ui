import { useRef, useState } from 'react'
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
  const [open, setOpen] = useState(false)
  const lockedRef = useRef(false)
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  function clearCloseTimer() {
    if (closeTimerRef.current) {
      clearTimeout(closeTimerRef.current)
      closeTimerRef.current = null
    }
  }

  function scheduleClose() {
    if (lockedRef.current) return
    closeTimerRef.current = setTimeout(() => setOpen(false), 150)
  }

  function handleTriggerClick() {
    if (!lockedRef.current) {
      lockedRef.current = true
      setOpen(true)
    } else {
      lockedRef.current = false
      setOpen(false)
    }
  }

  // Radix calls this when the trigger is clicked (toggle) or Escape/outside fires.
  // The ref is synchronous so handleTriggerClick's lock change is already visible here.
  function handleOpenChange(nextOpen: boolean) {
    if (!nextOpen && lockedRef.current) return
    if (nextOpen) setOpen(true)
  }

  function handleClose() {
    lockedRef.current = false
    setOpen(false)
  }

  return (
    <DropdownMenu open={open} onOpenChange={handleOpenChange}>
      <DropdownMenuTrigger
        asChild
        onClick={handleTriggerClick}
        onMouseEnter={() => { clearCloseTimer(); setOpen(true) }}
        onMouseLeave={scheduleClose}
      >
        {trigger}
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align={align}
        onMouseEnter={clearCloseTimer}
        onMouseLeave={scheduleClose}
        onInteractOutside={handleClose}
        onEscapeKeyDown={handleClose}
      >
        {children}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}