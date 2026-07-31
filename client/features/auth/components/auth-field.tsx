"use client";

import type { ComponentProps, ReactNode } from "react";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";

/**
 * Label + input + error, styled to the Obsidian input spec.
 *
 * The base shadcn Input is a compact 32px control intended for dense data UI.
 * Auth forms are the opposite context — low density, high stakes — so this
 * wrapper applies the spec from DESIGN.md "Components → Input Fields":
 * 10px radius, inset background (the canvas colour, darker than the panel it
 * sits on), and a 2px primary focus ring.
 *
 * `dark:bg-background` is not redundant: the base Input ships a
 * `dark:bg-input/30` rule, and since <html> always carries `dark`, that rule
 * would otherwise win over a plain `bg-background`.
 */

export const authInputClassName = cn(
  "h-auto w-full rounded-lg border border-border bg-background px-md py-sm text-body-md text-foreground",
  "placeholder:text-muted-foreground/50",
  "focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring",
  "dark:bg-background dark:aria-invalid:border-destructive",
  "aria-invalid:border-destructive aria-invalid:ring-0",
  "md:text-body-md"
);

type AuthFieldProps = ComponentProps<"input"> & {
  label: string;
  /** Rendered under the control. Accepts a node so callers can list rules. */
  error?: ReactNode;
  /** Rendered to the right of the label, e.g. a "Forgot password?" link. */
  action?: ReactNode;
};

export function AuthField({
  label,
  error,
  action,
  id,
  className,
  ...props
}: AuthFieldProps) {
  const errorId = error ? `${id}-error` : undefined;

  return (
    <div className="space-y-base">
      <div className="flex items-center justify-between gap-md">
        <Label
          htmlFor={id}
          className="text-label-md tracking-wider text-muted-foreground uppercase"
        >
          {label}
        </Label>
        {action}
      </div>

      <Input
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={errorId}
        className={cn(authInputClassName, className)}
        {...props}
      />

      {error ? (
        <div id={errorId} className="text-label-md text-destructive">
          {error}
        </div>
      ) : null}
    </div>
  );
}
