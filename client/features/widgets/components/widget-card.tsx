import Link from "next/link";

import { DeleteWidgetButton } from "@/features/widgets/components/delete-widget-button";
import { WidgetStatusBadge } from "@/features/widgets/components/widget-status-badge";
import { DEFAULT_ORIGIN_LABEL } from "@/features/widgets/schemas/widget-schema";
import { WIDGET_TYPE_LABELS, type Widget } from "@/features/widgets/types/widget";

const NO_ORIGIN_TITLE =
  "No origin registered — this widget may be embedded from any site.";

export function WidgetCard({ widget }: { widget: Widget }) {
  return (
    <div className="flex h-full flex-col rounded-lg border border-border bg-surface p-lg transition-colors hover:border-surface-hover">

      {/* Title */}
      <div className="mb-md flex items-start justify-between gap-sm">
        <h3
          title={widget.name}
          className="min-w-0 truncate text-headline-md text-foreground"
        >
          {widget.name}
        </h3>
        <WidgetStatusBadge status={widget.status} />
      </div>

      <div className="flex-1 space-y-sm">

        {/* Type */}
        <div className="flex items-center justify-between gap-md border-b border-border py-xs">
          <span className="text-label-md tracking-wide text-muted-foreground uppercase">
            Type
          </span>
          <span className="text-body-md text-foreground">
            {WIDGET_TYPE_LABELS[widget.type]}
          </span>
        </div>

        {/* Origin */}
        <div className="flex items-center justify-between gap-md border-b border-border py-xs">
          <span className="shrink-0 text-label-md tracking-wide text-muted-foreground uppercase">
            Origin
          </span>
          {widget.origin ? (
            <span
              title={widget.origin}
              className="min-w-0 truncate font-mono text-mono-label text-foreground"
            >
              {widget.origin}
            </span>
          ) : (
            // The brief asks for the product host as the visible fallback; the
            // tooltip keeps it honest about what a missing origin means.
            <span
              title={NO_ORIGIN_TITLE}
              className="min-w-0 truncate font-mono text-mono-label text-muted-foreground"
            >
              {DEFAULT_ORIGIN_LABEL}
            </span>
          )}
        </div>
      </div>

      {/* Action buttons */}
      <div className="mt-md flex items-center justify-end border-t border-border pt-md">
        <div className="flex items-center gap-xs">
          <Link
            href={`/dashboard/widgets/${widget.widgetId}`}
            className="rounded-[6px] border border-border px-sm py-1 text-[11px] transition-colors hover:bg-surface-hover hover:text-foreground"
          >
            Quick Edit
          </Link>
          <DeleteWidgetButton widgetId={widget.widgetId} name={widget.name} />
        </div>
      </div>
    </div>
  );
}
