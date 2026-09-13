import { Plus } from "lucide-react";
import Link from "next/link";

import { WidgetCard } from "@/features/widgets/components/widget-card";
import type { Widget } from "@/features/widgets/types/widget";

export function WidgetList({
  widgets,
  filtered,
  clearHref,
}: {
  widgets: Widget[];
  filtered: boolean;
  clearHref: string;
}) {
  if (widgets.length === 0) {
    return (
      <div className="flex flex-col items-center gap-md rounded-xl border border-dashed border-border px-md py-2xl text-center">
        {filtered ? (
          <>
            <p className="text-body-md text-muted-foreground">
              No widgets match these filters.
            </p>
            <Link
              href={clearHref}
              className="inline-flex h-9 items-center rounded-lg border border-border px-md text-label-md text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground"
            >
              Clear filters
            </Link>
          </>
        ) : (
          <>
            <div className="space-y-xs">
              <p className="text-body-lg text-foreground">No widgets yet</p>
              <p className="text-body-md text-muted-foreground">
                Create a widget to take bookings from your website.
              </p>
            </div>
            <Link
              href="/dashboard/widgets/new"
              className="inline-flex items-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90"
            >
              <Plus className="size-4" aria-hidden="true" />
              Create Widget
            </Link>
          </>
        )}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-lg md:grid-cols-2 xl:grid-cols-3">
      {widgets.map((widget) => (
        <WidgetCard key={widget.widgetId} widget={widget} />
      ))}
    </div>
  );
}
