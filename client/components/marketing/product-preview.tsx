import { cn } from "@/lib/utils";

/**
 * Decorative dashboard mock shown under the hero.
 *
 * Drawn in the DOM rather than shipped as a screenshot: the real dashboard is
 * still being built, so a screenshot would be stale the day it landed. Nothing
 * here is live data — the whole frame is aria-hidden.
 */
export function ProductPreview() {
  return (
    <div
      aria-hidden="true"
      className="relative mx-auto mt-2xl max-w-[1024px] overflow-hidden rounded-xl border border-border bg-surface-container shadow-[0_20px_40px_rgba(0,0,0,0.4)] lg:mt-[80px]"
    >
      {/* Browser chrome */}
      <div className="flex h-8 items-center gap-xs border-b border-border bg-surface px-md">
        <span className="size-2.5 rounded-full bg-surface-hover" />
        <span className="size-2.5 rounded-full bg-surface-hover" />
        <span className="size-2.5 rounded-full bg-surface-hover" />
      </div>

      <div className="flex">
        {/* Sidebar rail */}
        <div className="hidden w-[160px] shrink-0 flex-col gap-xs border-r border-border bg-surface p-sm sm:flex">
          <div className="mb-xs h-2 w-2/3 rounded-full bg-surface-hover" />
          {[0, 1, 2, 3, 4, 5].map((row) => (
            <div key={row} className="flex items-center gap-xs py-base">
              <span className="size-3 rounded-sm bg-surface-hover" />
              <span className="h-2 flex-1 rounded-full bg-surface-hover/60" />
            </div>
          ))}
        </div>

        <div className="min-w-0 flex-1 p-sm">
          {/* Metric tiles */}
          <div className="grid grid-cols-3 gap-xs">
            <MetricTile label="Total Bookings" value="7,219" accent="primary" />
            <MetricTile label="Revenue" value="$482.9K" accent="success" />
            <MetricTile label="Occupancy" value="88%" accent="warning" />
          </div>

          {/* Calendar grid of booking blocks */}
          <div className="mt-xs rounded-lg border border-border bg-surface p-sm">
            <div className="mb-xs flex gap-xs">
              {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((day) => (
                <span
                  key={day}
                  className="flex-1 text-center text-mono-label font-mono text-muted-foreground/70"
                >
                  {day}
                </span>
              ))}
            </div>
            <div className="grid grid-cols-7 gap-xs">
              {BOOKING_BLOCKS.map((block, index) => (
                <span key={index} className={cn("h-4 rounded-sm", block)} />
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Fades the mock into the canvas at its lower edge */}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-background via-transparent to-transparent" />
    </div>
  );
}

function MetricTile({
  label,
  value,
  accent,
}: {
  label: string;
  value: string;
  accent: "primary" | "success" | "warning";
}) {
  const bar = {
    primary: "bg-primary/40",
    success: "bg-success/40",
    warning: "bg-warning/40",
  }[accent];

  return (
    <div className="rounded-lg border border-border bg-surface p-sm">
      <p className="text-mono-label font-mono text-muted-foreground/70">
        {label}
      </p>
      <p className="mt-base text-body-lg font-semibold text-foreground">
        {value}
      </p>
      <div className="mt-xs flex h-6 items-end gap-px">
        {[40, 65, 30, 80, 55, 90, 45, 70].map((height, index) => (
          <span
            key={index}
            className={cn("flex-1 rounded-t-sm", bar)}
            style={{ height: `${height}%` }}
          />
        ))}
      </div>
    </div>
  );
}

/** Fake occupancy pattern — an empty column reads as a free slot. */
const BOOKING_BLOCKS = [
  "bg-primary/30",
  "bg-success/25",
  "bg-transparent",
  "bg-warning/25",
  "bg-primary/30",
  "bg-transparent",
  "bg-success/25",
  "bg-transparent",
  "bg-primary/25",
  "bg-success/30",
  "bg-transparent",
  "bg-warning/20",
  "bg-primary/20",
  "bg-transparent",
  "bg-warning/25",
  "bg-transparent",
  "bg-primary/30",
  "bg-success/20",
  "bg-transparent",
  "bg-primary/25",
  "bg-success/25",
];
