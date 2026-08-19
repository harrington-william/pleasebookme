import { ArrowRight } from "lucide-react";
import Link from "next/link";

import { ProductPreview } from "@/components/marketing/product-preview";

export function HeroSection() {
  return (
    <section className="relative mx-auto max-w-[1440px] overflow-hidden px-lg pt-2xl pb-xl text-center lg:px-2xl lg:pt-[120px] lg:pb-[80px]">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute top-0 left-1/2 h-[400px] w-[800px] -translate-x-1/2 rounded-full bg-primary/5 blur-[120px]"
      />

      <div className="relative z-10 mx-auto max-w-[800px]">
        <h1 className="mb-lg text-headline-lg tracking-tight text-balance text-foreground md:text-display">
          Booking infrastructure for modern service businesses.
        </h1>

        <p className="mx-auto mb-xl max-w-[600px] text-body-lg text-muted-foreground">
          Manage bookings, schedules, customers, resources and business
          operations from one centralized platform. Built for performance,
          designed for scale.
        </p>

        <div className="flex flex-col items-center justify-center gap-md sm:flex-row">
          <Link
            href="/register"
            className="inline-flex w-full items-center justify-center gap-xs rounded-lg bg-primary px-lg py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/80 sm:w-auto"
          >
            Start Building Free
            <ArrowRight className="size-4" aria-hidden="true" />
          </Link>

          {/* Reserved: no documentation site exists yet, so this stays inert. */}
          <span
            aria-disabled="true"
            title="No documentation site exists yet."
            className="inline-flex w-full cursor-default items-center justify-center rounded-lg border border-border px-lg py-sm text-label-md text-muted-foreground/50 sm:w-auto"
          >
            Read the Docs
          </span>
        </div>
      </div>

      <ProductPreview />
    </section>
  );
}
