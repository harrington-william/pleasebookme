import type { DashboardMetrics } from "@/features/dashboard/types/dashboard";

export function DashboardMetricTiles({
  metrics,
}: {
  metrics: DashboardMetrics;
}) {
  return (
    <div className="grid grid-cols-1 gap-md sm:grid-cols-2 xl:grid-cols-4">
      <StatTile
        label="Today's Bookings"
        value={metrics.todaysBookings.toLocaleString()}
      />
      <StatTile label="Pending" value={metrics.pending.toLocaleString()} />
      <StatTile
        label="Cancellations"
        value={metrics.cancellations.toLocaleString()}
      />

      {/*
        Reserved: core.bookings has no amount or currency column, and the
        billing schema is outside PLATFORM V1.0.0.
      */}
      <div
        className="rounded-xl border border-dashed border-border bg-surface/60 p-md opacity-60"
        aria-disabled="true"
      >
        <p className="text-label-md tracking-wider text-muted-foreground uppercase">
          Revenue
        </p>
        <p className="mt-xs text-headline-md text-muted-foreground">—</p>
        <p className="mt-xs text-label-md text-muted-foreground">
          Needs booking price data
        </p>
      </div>
    </div>
  );
}

function StatTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-border bg-surface p-md">
      <p className="text-label-md tracking-wider text-muted-foreground uppercase">
        {label}
      </p>
      <p className="mt-xs text-headline-md text-foreground">{value}</p>
    </div>
  );
}
