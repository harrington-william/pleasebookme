import { ServiceCard } from "@/features/services/components/service-card";
import type { ServiceCatalogEntry } from "@/features/services/types/service";

export function ServiceCatalogList({
  entries,
}: {
  entries: ServiceCatalogEntry[];
}) {
  if (entries.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-border px-md py-2xl text-center">
        <p className="text-body-md text-muted-foreground">
          No services yet. Create one to start accepting bookings.
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-lg md:grid-cols-2 xl:grid-cols-3">
      {entries.map((entry) => (
        <ServiceCard key={entry.service.serviceId} entry={entry} />
      ))}
    </div>
  );
}
