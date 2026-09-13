import type { WidgetStatus } from "@/features/widgets/types/widget";
import { cn } from "@/lib/utils";

// Total over the enum even though the list only ever shows two of them, so a
// stale link or a future filter never renders an unstyled badge.
const statusStyles: Record<WidgetStatus, string> = {
  ACTIVE: "border-success/30 bg-success/10 text-success",
  DISABLED: "border-warning/30 bg-warning/10 text-warning",
  REGISTERING: "border-primary/30 bg-primary/10 text-primary",
  REVOKED: "border-border bg-muted/40 text-muted-foreground",
};

export function WidgetStatusBadge({ status }: { status: WidgetStatus }) {
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
