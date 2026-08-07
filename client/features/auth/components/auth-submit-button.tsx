"use client";

import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

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
