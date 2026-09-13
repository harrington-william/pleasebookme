import type { ResourceStats } from "@/features/resources/types/resource";

export function ResourceStatsTiles({ stats }: { stats: ResourceStats }) {
  return (
    <div className="grid grid-cols-1 gap-md md:grid-cols-3">
      <StatTile
        label="Total Resources"
        value={stats.total.toLocaleString()}
        detail={`${stats.retired.toLocaleString()} retired`}
      />
      <StatTile
        label="Active Assets"
        value={stats.active.toLocaleString()}
        detail={`${stats.maintenance.toLocaleString()} in maintenance, ${stats.inactive.toLocaleString()} inactive`}
      />

      {/*
        Reserved: utilization needs booking data joined against resource
        availability. Neither the query nor an agreed definition exists yet, and
        a number derived from the counts above would look authoritative while
        measuring nothing.
      */}
      <div
        className="rounded-xl border border-dashed border-border bg-surface/60 p-md opacity-60"
        aria-disabled="true"
      >
        <p className="text-label-md tracking-wider text-muted-foreground uppercase">
          Utilization Rate
        </p>
        <p className="mt-xs text-headline-md text-muted-foreground">—</p>
        <p className="mt-xs text-label-md text-muted-foreground">
          Needs booking data
        </p>
      </div>
    </div>
  );
}

function StatTile({
  label,
  value,
  detail,
}: {
  label: string;
  value: string;
  detail: string;
}) {
  return (
    <div className="rounded-xl border border-border bg-surface p-md">
      <p className="text-label-md tracking-wider text-muted-foreground uppercase">
        {label}
      </p>
      <p className="mt-xs text-headline-md text-foreground">{value}</p>
      <p className="mt-xs text-label-md text-muted-foreground">{detail}</p>
    </div>
  );
}
