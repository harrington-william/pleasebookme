"use client";

import type { PublicServiceSummary } from "@/features/public-booking/types/public-booking";

import { ServiceCard } from "./service-card";

export function ServicePicker({
  services,
  selectedSlug,
  loadingSlug,
  onSelect,
}: {
  services: PublicServiceSummary[];
  selectedSlug: string | null;
  loadingSlug: string | null;
  onSelect: (service: PublicServiceSummary) => void;
}) {
  if (!services.length) {
    return (
      <div className="rounded-xl border border-border bg-surface p-lg text-body-md text-muted-foreground">
        No services available for this business yet.
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-md">
      {services.map((service) => (
        <ServiceCard
          key={service.slug}
          service={service}
          selected={service.slug === selectedSlug}
          loading={service.slug === loadingSlug}
          disabled={loadingSlug !== null}
          onSelect={onSelect}
        />
      ))}
    </div>
  );
}
