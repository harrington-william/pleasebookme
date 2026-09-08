"use client";

import type { ReactNode } from "react";

import { Label } from "@/components/ui/label";

export const inputClassName =
  "h-9 w-full rounded-lg border border-border bg-background px-md text-body-md text-foreground placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none";

export function Field({
  label,
  htmlFor,
  hint,
  error,
  children,
}: {
  label: string;
  htmlFor?: string;
  hint?: ReactNode;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div className="flex flex-col">
      <Label
        htmlFor={htmlFor}
        className="mb-sm text-label-md tracking-wider text-muted-foreground uppercase"
      >
        {label}
      </Label>
      {children}
      {error ? (
        <p className="mt-base text-label-md text-destructive">{error}</p>
      ) : hint ? (
        <p className="mt-base text-label-md text-muted-foreground">{hint}</p>
      ) : null}
    </div>
  );
}

export function Section({
  id,
  title,
  children,
}: {
  id: string;
  title: string;
  children: ReactNode;
}) {
  return (
    <section
      id={id}
      className="scroll-mt-lg space-y-md rounded-xl border border-border bg-surface p-lg"
    >
      <h2 className="text-headline-md text-foreground">{title}</h2>
      {children}
    </section>
  );
}
