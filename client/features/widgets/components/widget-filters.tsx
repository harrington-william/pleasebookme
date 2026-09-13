"use client";

import { X } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import { useTransition } from "react";

import { NativeSelect } from "@/components/form/native-select";
import {
  buildWidgetSearchParams,
  hasActiveWidgetFilters,
} from "@/features/widgets/schemas/widget-query";
import {
  SUPPORTED_WIDGET_TYPES,
  WIDGET_EDITABLE_STATUSES,
  WIDGET_TYPE_LABELS,
  type WidgetEditableStatus,
  type WidgetQuery,
  type WidgetType,
} from "@/features/widgets/types/widget";
import { cn } from "@/lib/utils";

function toTitleCase(value: string): string {
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export function WidgetFilters({
  query,
  totalElements,
}: {
  query: WidgetQuery;
  totalElements: number;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [isPending, startTransition] = useTransition();

  function navigate(overrides: Partial<WidgetQuery>) {
    const search = buildWidgetSearchParams(query, { page: 0, ...overrides });
    startTransition(() => router.push(`${pathname}${search}`));
  }

  return (
    <div
      className={cn(
        "flex flex-col gap-sm rounded-xl border border-border bg-surface p-md lg:flex-row lg:items-center",
        isPending && "opacity-70"
      )}
    >
      {/*
        Only the types this client can create are offered. The platform accepts
        four, but a filter for a type no widget can have would only ever return
        an empty page.
      */}
      <NativeSelect
        value={query.type ?? ""}
        onChange={(event) =>
          navigate({
            type: (event.target.value || undefined) as WidgetType | undefined,
          })
        }
        aria-label="Filter by type"
        containerClassName="w-full lg:w-45"
      >
        <option value="">All types</option>

        {SUPPORTED_WIDGET_TYPES.map((type) => (
          <option key={type} value={type}>
            {WIDGET_TYPE_LABELS[type]}
          </option>
        ))}
      </NativeSelect>

      {/* Revoked is not offered: the list endpoint never returns those rows. */}
      <NativeSelect
        value={query.status ?? ""}
        onChange={(event) =>
          navigate({
            status: (event.target.value || undefined) as
              | WidgetEditableStatus
              | undefined,
          })
        }
        aria-label="Filter by status"
        containerClassName="w-full lg:w-45"
      >
        <option value="">All statuses</option>

        {WIDGET_EDITABLE_STATUSES.map((status) => (
          <option key={status} value={status}>
            {toTitleCase(status)}
          </option>
        ))}
      </NativeSelect>

      {hasActiveWidgetFilters(query) ? (
        <button
          type="button"
          onClick={() => navigate({ type: undefined, status: undefined })}
          className="inline-flex h-9 items-center justify-center gap-xs rounded-lg border border-border px-md text-label-md text-muted-foreground transition-colors hover:bg-surface-hover"
        >
          <X className="size-4" aria-hidden="true" />
          Clear
        </button>
      ) : null}

      <p className="text-label-md text-muted-foreground lg:ml-auto">
        {totalElements.toLocaleString()}{" "}
        {totalElements === 1 ? "widget" : "widgets"}
      </p>
    </div>
  );
}
