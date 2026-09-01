import type { ResourceStatus } from "@/features/resources/types/resource";
import { cn } from "@/lib/utils";

const statusStyles: Record<ResourceStatus, string> = {
  ACTIVE: "border-success/30 bg-success/10 text-success",
  INACTIVE: "border-primary/30 bg-primary/10 text-primary",
  MAINTENANCE: "border-warning/30 bg-warning/10 text-warning",
  RETIRED: "border-border bg-muted/40 text-muted-foreground",
};

export function ResourceStatusBadge({ status }: { status: ResourceStatus }) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-sm border px-xs py-base text-label-md",
        statusStyles[status]
      )}
    >
      {status.charAt(0) + status.slice(1).toLowerCase()}
    </span>
  );
}
