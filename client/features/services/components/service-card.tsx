import { Sparkles } from "lucide-react";
import Link from "next/link";

import { DeleteServiceButton } from "@/features/services/components/delete-service-button";
import {
  formatDurationAmount,
  fromMinutes,
} from "@/features/services/schemas/duration-unit";
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

  const notice = fromMinutes(policy.minimumNotice);
  const noticeLabel =
    policy.minimumNotice > 0
      ? `${formatDurationAmount(notice.amount, notice.unit)} notice`
      : "No notice required";

  return `${noticeLabel} · ${policy.capacity} seat${policy.capacity === 1 ? "" : "s"}`;
}

export function ServiceCard({ entry }: { entry: ServiceCatalogEntry }) {
  const { service } = entry;

  return (
    <div className="flex h-full flex-col rounded-lg border border-border bg-surface p-lg transition-colors hover:border-surface-hover">

      {/* Title */}
      <div className="mb-md flex items-start justify-between gap-sm">
        <div className="flex items-center gap-sm">
          <div>
            <h3 className="text-headline-md text-foreground">
              {service.title}
            </h3>
            <div
              className="mt-1 flex items-center gap-xs"
              title="core.services has no status column yet — nothing to report here."
            >
              <span className="size-1.5 rounded-full bg-muted-foreground/50" />
              <span className="font-mono text-mono-label text-green-500">
                Active
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 space-y-sm">

        {/* Duration */}
        <div className="flex items-center justify-between border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Duration
          </span>
          <span className="text-body-md text-foreground">
            {formatDuration(entry)}
          </span>
        </div>

        {/* Price */}
        <div className="flex items-center justify-between border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Price
          </span>
          <span className="text-body-md text-foreground">
            {formatPrice(entry)}
          </span>
        </div>

        {/* Policy */}
        <div className="flex items-center justify-between border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Policy
          </span>
          <span className="text-body-md text-foreground">
            {formatPolicy(entry)}
          </span>
        </div>
      </div>

      {/* Action buttons */}
      <div className="mt-md flex justify-end items-center border-t border-border pt-md">
        <div className="flex items-center gap-xs">
          <Link
            href={`/dashboard/services/${service.serviceId}`}
            className="rounded-[6px] border border-border px-sm py-1 text-[11px] transition-colors hover:bg-surface-hover hover:text-foreground"
          >
            Quick Edit
          </Link>
          <DeleteServiceButton serviceId={service.serviceId} title={service.title} />
        </div>
      </div>
    </div>
  );
}
