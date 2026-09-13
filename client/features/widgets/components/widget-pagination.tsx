import Link from "next/link";

import { buildWidgetSearchParams } from "@/features/widgets/schemas/widget-query";
import type { WidgetPageMeta, WidgetQuery } from "@/features/widgets/types/widget";
import { cn } from "@/lib/utils";

const linkClassName =
  "inline-flex h-9 items-center rounded-lg border border-border px-md text-label-md transition-colors";

export function WidgetPagination({
  query,
  page,
}: {
  query: WidgetQuery;
  page: WidgetPageMeta;
}) {
  if (page.totalPages <= 1) {
    return null;
  }

  const firstIndex = page.page * page.size + 1;
  const lastIndex = Math.min(firstIndex + page.size - 1, page.totalElements);
  const hasPrevious = page.page > 0;
  const hasNext = page.page + 1 < page.totalPages;

  return (
    <div className="flex flex-col items-center justify-between gap-sm rounded-xl border border-border bg-surface px-md py-sm sm:flex-row">
      <p className="text-label-md text-muted-foreground">
        Showing {firstIndex.toLocaleString()} to {lastIndex.toLocaleString()} of{" "}
        {page.totalElements.toLocaleString()} widgets
      </p>

      <div className="flex items-center gap-xs">
        <PageLink
          href={buildWidgetSearchParams(query, { page: page.page - 1 })}
          enabled={hasPrevious}
          label="Previous"
        />
        <span className="px-sm text-label-md text-muted-foreground">
          Page {page.page + 1} of {page.totalPages}
        </span>
        <PageLink
          href={buildWidgetSearchParams(query, { page: page.page + 1 })}
          enabled={hasNext}
          label="Next"
        />
      </div>
    </div>
  );
}

function PageLink({
  href,
  enabled,
  label,
}: {
  href: string;
  enabled: boolean;
  label: string;
}) {
  if (!enabled) {
    return (
      <span
        aria-disabled="true"
        className={cn(
          linkClassName,
          "cursor-not-allowed text-muted-foreground opacity-50"
        )}
      >
        {label}
      </span>
    );
  }

  return (
    <Link
      href={href || "?"}
      className={cn(linkClassName, "text-foreground hover:bg-surface-hover")}
    >
      {label}
    </Link>
  );
}
