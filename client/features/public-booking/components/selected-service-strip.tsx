import { formatDuration, formatPriceRange } from "@/features/public-booking/lib/format";
import type { PublicService } from "@/features/public-booking/types/public-booking";
import { cn } from "@/lib/utils";

export function SelectedServiceStrip({
  service,
  className,
}: {
  service: PublicService;
  className?: string;
}) {
  return (
    <p className={cn("-mt-md mb-lg text-body-md text-muted-foreground", className)}>
      {service.title} · {formatDuration(service.durationMinutes)} · {formatPriceRange(service.minPrice, service.maxPrice, service.currency)}
    </p>
  );
}
