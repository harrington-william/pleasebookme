"use client";

import { Search, X } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import { useTransition } from "react";

import { Input } from "@/components/ui/input";
import {
  buildResourceSearchParams,
  hasActiveResourceFilters,
} from "@/features/resources/schemas/resource-query";
import {
  RESOURCE_STATUSES,
  type ResourceQuery,
  type ResourceStatus,
  type ResourceType,
} from "@/features/resources/types/resource";
import { cn } from "@/lib/utils";

const selectClassName =
  "h-9 rounded-lg border border-border bg-background px-md text-body-md text-foreground";

function toTitleCase(value: string): string {
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export function ResourceFilters({
  query,
  resourceTypes,
}: {
  query: ResourceQuery;
  resourceTypes: ResourceType[];
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [isPending, startTransition] = useTransition();

  function navigate(overrides: Partial<ResourceQuery>) {
    // Any filter change invalidates the current offset, so always return to page 0.
    const search = buildResourceSearchParams(query, { page: 0, ...overrides });
    startTransition(() => router.push(`${pathname}${search}`));
  }

  function onSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const submitted = new FormData(event.currentTarget).get("q");
    const text = String(submitted ?? "").trim();
    navigate({ q: text || undefined });
  }

  return (
    <form
      onSubmit={onSearchSubmit}
      className={cn(
        "flex flex-col gap-sm rounded-xl border border-border bg-surface p-md lg:flex-row lg:items-center",
        isPending && "opacity-70"
      )}
    >
      <div className="relative min-w-0 flex-1">
        <Search
          className="pointer-events-none absolute top-1/2 left-sm size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />
        {/*
          Uncontrolled + keyed on the URL value: the field stays local while
          typing, and a URL change from elsewhere (Clear, back/forward) remounts
          it with the correct value instead of fighting the user's input.
        */}
        <Input
          key={query.q ?? ""}
          name="q"
          defaultValue={query.q ?? ""}
          aria-label="Search resources by name or slug"
          placeholder="Search resources, then press Enter…"
          className="h-9 bg-background pl-9 text-body-md"
        />
      </div>

      <select
        value={query.resourceTypeId ? String(query.resourceTypeId) : ""}
        onChange={(event) =>
          navigate({
            resourceTypeId: event.target.value
              ? Number(event.target.value)
              : undefined,
          })
        }
        aria-label="Filter by resource type"
        className={selectClassName}
      >
        <option value="">All types</option>
        {resourceTypes.map((resourceType) => (
          <option
            key={resourceType.resourceTypeId}
            value={String(resourceType.resourceTypeId)}
          >
            {resourceType.name}
          </option>
        ))}
      </select>

      <select
        value={query.status ?? ""}
        onChange={(event) =>
          navigate({
            status: (event.target.value || undefined) as
              | ResourceStatus
              | undefined,
          })
        }
        aria-label="Filter by status"
        className={selectClassName}
      >
        <option value="">All statuses</option>
        {RESOURCE_STATUSES.map((status) => (
          <option key={status} value={status}>
            {toTitleCase(status)}
          </option>
        ))}
      </select>

      <button type="submit" className="sr-only">
        Search
      </button>

      {hasActiveResourceFilters(query) ? (
        <button
          type="button"
          onClick={() =>
            navigate({ q: undefined, resourceTypeId: undefined, status: undefined })
          }
          className="inline-flex h-9 items-center justify-center gap-xs rounded-lg border border-border px-md text-label-md text-muted-foreground transition-colors hover:bg-surface-hover"
        >
          <X className="size-4" aria-hidden="true" />
          Clear
        </button>
      ) : null}
    </form>
  );
}
