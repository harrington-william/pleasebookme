import { formatDuration, formatPriceRange } from "../../../../../core/lib/format";
import type { Service } from "../../../logic/types";
import { cn } from "../../../../../core/lib/cn";

export function SelectedServiceStrip({
  service,
  className,
}: {
  service: Service;
  className?: string;
}) {
  return (
    <p className={cn("-mt-md mb-lg text-body-md text-muted-foreground", className)}>
      {service.title} · {formatDuration(service.durationMinutes)} · {formatPriceRange(service.minPrice, service.maxPrice, service.currency)}
    </p>
  );
}
