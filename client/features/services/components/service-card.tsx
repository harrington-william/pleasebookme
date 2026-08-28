import { Sparkles } from "lucide-react";

import { DeleteServiceButton } from "@/features/services/components/delete-service-button";
import type { ServiceCatalogEntry } from "@/features/services/types/service";

const CURRENCY_SYMBOLS: Record<string, string> = {
  USD: "$",
  AUD: "A$",
  SGD: "S$",
  GBP: "£",
  VND: "₫",
};

function formatPrice(entry: ServiceCatalogEntry): string {
  const { minPrice, currency } = entry.service;
  if (minPrice === null) return "TBD";
  const symbol = CURRENCY_SYMBOLS[currency] ?? currency;
  return `${symbol}${minPrice.toFixed(2)}`;
}

function formatDuration(entry: ServiceCatalogEntry): string {
  const policy = entry.bookingPolicy;
  if (!policy) return "Not configured";

  return `${policy.defaultDuration} min`;
}

function formatPolicy(entry: ServiceCatalogEntry): string {
  const policy = entry.bookingPolicy;
  if (!policy) return "Not configured";

  // minimumNotice has no documented unit at the DB level (plain INTEGER) —
  // treated as hours throughout this feature, see CreateServiceForm.
  const noticeLabel =
    policy.minimumNotice > 0
      ? `${policy.minimumNotice}h notice`
      : "No notice required";

  return `${noticeLabel} · ${policy.capacity} seat${policy.capacity === 1 ? "" : "s"}`;
}

export function ServiceCard({ entry }: { entry: ServiceCatalogEntry }) {
  const { service } = entry;

  return (
    <div className="flex h-full flex-col rounded-lg border border-border bg-surface p-lg transition-colors hover:border-surface-hover">
      <div className="mb-md flex items-start justify-between gap-sm">
        <div className="flex items-center gap-sm">
          <div className="flex size-10 shrink-0 items-center justify-center rounded-[10px] border border-border bg-surface-container">
            <Sparkles className="size-5 text-primary" aria-hidden="true" />
          </div>
          <div>
            <h3 className="text-headline-md text-foreground">
              {service.title}
            </h3>
            <div
              className="mt-1 flex items-center gap-xs"
              title="core.services has no status column yet — nothing to report here."
            >
              <span className="size-1.5 rounded-full bg-muted-foreground/50" />
              <span className="font-mono text-mono-label text-muted-foreground">
                Not tracked
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 space-y-sm">
        <div className="flex items-center justify-between border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Duration
          </span>
          <span className="text-body-md text-foreground">
            {formatDuration(entry)}
          </span>
        </div>
        <div className="flex items-center justify-between border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Price
          </span>
          <span className="text-body-md text-foreground">
            {formatPrice(entry)}
          </span>
        </div>
        <div className="flex items-center justify-between border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Policy
          </span>
          <span className="text-body-md text-foreground">
            {formatPolicy(entry)}
          </span>
        </div>
      </div>

      <div className="mt-md flex items-center justify-between border-t border-border pt-md">
        <span
          className="font-mono text-[11px] text-muted-foreground"
          title="Requires assigning a Resource to this service, then a staff Membership to that resource (resource.resources.service_id -> resource.resource_assignments) — the Resources feature has not been built yet."
        >
          Resources not tracked
        </span>

        <div className="flex items-center gap-xs">
          <button
            type="button"
            disabled
            title="Editing a service has not been built yet."
            className="rounded-[6px] border border-border px-sm py-1 text-[11px] text-muted-foreground opacity-50"
          >
            Quick Edit
          </button>
          <DeleteServiceButton serviceId={service.serviceId} title={service.title} />
        </div>
      </div>
    </div>
  );
}
