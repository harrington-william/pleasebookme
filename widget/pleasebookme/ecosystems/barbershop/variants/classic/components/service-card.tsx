"use client";

import { Check, Clock, Zap } from "lucide-react";

import { formatDuration, formatPriceRange } from "../../../../../core/lib/format";
import type { ServiceSummary } from "../../../logic/types";
import { cn } from "../../../../../core/lib/cn";

export function ServiceCard({
  service,
  selected,
  loading,
  disabled,
  onSelect,
}: {
  service: ServiceSummary;
  selected: boolean;
  loading: boolean;
  disabled: boolean;
  onSelect: (service: ServiceSummary) => void;
}) {
  return (
    <button
      type="button"
      disabled={disabled}
      aria-busy={loading}
      aria-pressed={selected}
      onClick={() => onSelect(service)}
      className={cn(
        "flex w-full cursor-pointer items-center justify-between gap-md rounded-xl border border-border bg-surface p-md text-left shadow-sm transition-colors hover:border-muted-foreground/60 hover:bg-surface-hover focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none disabled:cursor-wait disabled:opacity-70",
        selected && "border-primary"
      )}
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-sm">
          <h2 className="text-headline-sm">{service.title}</h2>
          {service.autoConfirm ? (
            <span className="flex items-center gap-base rounded-full bg-primary/15 px-xs py-base text-label-md text-primary">
              <Zap className="size-3.5" aria-hidden="true" /> Instant booking
            </span>
          ) : null}
        </div>
        {service.description ? (
          <p className="mt-xs line-clamp-2 text-body-md text-muted-foreground">
            {service.description}
          </p>
        ) : null}
        <p className="mt-sm flex items-center gap-xs text-body-md text-muted-foreground">
          <Clock className="size-4" aria-hidden="true" />
          {formatDuration(service.durationMinutes)}
        </p>
      </div>
      <div className="flex shrink-0 items-center gap-md">
        <span className="text-headline-lg-mobile font-semibold">
          {formatPriceRange(service.minPrice, service.maxPrice, service.currency)}
        </span>
        {loading ? (
          <span
            className="size-6 animate-spin rounded-full border-2 border-border border-t-primary"
            aria-hidden="true"
          />
        ) : selected ? (
          <span className="flex size-6 items-center justify-center rounded-full bg-primary text-primary-foreground">
            <Check className="size-4" aria-hidden="true" />
          </span>
        ) : null}
      </div>
    </button>
  );
}
