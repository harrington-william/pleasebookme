"use client";

import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

/**
 * Primary action for the auth forms.
 *
 * Not built on components/ui/button.tsx: that button is sized for dense
 * application chrome (32px tall, small radius) whereas the auth screens call
 * for a full-width 10px-radius control per DESIGN.md. Wrapping and overriding
 * most of the shared button's variants would obscure more than it reuses.
 */

type AuthSubmitButtonProps = {
  isSubmitting: boolean;
  children: ReactNode;
  className?: string;
};

export function AuthSubmitButton({
  isSubmitting,
  children,
  className,
}: AuthSubmitButtonProps) {
  return (
    <button
      type="submit"
      disabled={isSubmitting}
      className={cn(
        "flex w-full items-center justify-center gap-xs rounded-lg bg-primary py-sm text-label-md text-primary-foreground transition-colors",
        "hover:bg-primary/90",
        "focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-surface focus-visible:outline-none",
        "disabled:cursor-not-allowed disabled:opacity-70",
        className
      )}
    >
      {children}
    </button>
  );
}
