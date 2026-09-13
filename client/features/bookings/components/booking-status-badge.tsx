import type { BookingStatus } from "@/features/bookings/types/booking";
import { cn } from "@/lib/utils";

const statusStyles: Record<BookingStatus, string> = {
  ACCEPTED: "border-success/30 bg-success/10 text-success",
  PENDING: "border-warning/30 bg-warning/10 text-warning",
  AWAITING_HOST: "border-warning/30 bg-warning/10 text-warning",
  CANCELLED: "border-destructive/30 bg-destructive/10 text-destructive",
  REJECTED: "border-border bg-muted/40 text-muted-foreground",
};

const statusLabels: Record<BookingStatus, string> = {
  ACCEPTED: "Confirmed",
  PENDING: "Pending",
  AWAITING_HOST: "Awaiting Host",
  CANCELLED: "Cancelled",
  REJECTED: "Rejected",
};

export function BookingStatusBadge({ status }: { status: BookingStatus }) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-sm border px-xs py-base text-label-md",
        statusStyles[status]
      )}
    >
      {statusLabels[status]}
    </span>
  );
}
