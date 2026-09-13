import type { WidgetStats } from "@/features/widgets/types/widget";

export function WidgetStatsTiles({ stats }: { stats: WidgetStats }) {
  return (
    <div className="grid grid-cols-1 gap-md sm:grid-cols-2">
      <StatTile
        label="Active Widgets"
        value={stats.active.toLocaleString()}
        detail={`of ${stats.total.toLocaleString()} total`}
      />

      {/*
        Revoked widgets are soft-deleted and never appear in the list; this
        count is the only trace of them. Hiding it would make active + disabled
        look like it should equal total when it does not.
      */}
      <StatTile
        label="Disabled"
        value={stats.disabled.toLocaleString()}
        detail={`${stats.revoked.toLocaleString()} revoked`}
      />
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
