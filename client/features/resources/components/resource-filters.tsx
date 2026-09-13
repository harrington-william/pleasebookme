"use client";

import { Search, X } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import { useTransition } from "react";

import { NativeSelect } from "@/components/form/native-select";
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

        {/* Search bar */}
        <Search
          className="pointer-events-none absolute top-1/2 left-sm size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />
        <Input
          key={query.q ?? ""}
          name="q"
          defaultValue={query.q ?? ""}
          aria-label="Search resources by name or slug"
          placeholder="Search"
          className="h-9 bg-background pl-9 text-body-md"
        />
      </div>

      {/* Filter by resource type */}
      <NativeSelect
        value={query.resourceTypeId ? String(query.resourceTypeId) : ""}
        onChange={(event) =>
          navigate({
            resourceTypeId: event.target.value
              ? Number(event.target.value)
              : undefined,
          })
        }
        aria-label="Filter by resource type"
        className="w-full xl:max-w-37.5"
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
      </NativeSelect>

      {/* Filter by status */}
      <NativeSelect
        value={query.status ?? ""}
        onChange={(event) =>
          navigate({
            status: (event.target.value || undefined) as
              | ResourceStatus
              | undefined,
          })
        }
        aria-label="Filter by status"
        className="w-full xl:max-w-37.5"
      >
        <option value="">All statuses</option>

        {RESOURCE_STATUSES.map((status) => (
          <option key={status} value={status}>
            {toTitleCase(status)}
          </option>
        ))}
      </NativeSelect>

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
