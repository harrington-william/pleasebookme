"use client";

import { Search, X } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import { useTransition } from "react";

import { NativeSelect } from "@/components/form/native-select";
import { Input } from "@/components/ui/input";
import {
  buildBookingSearchParams,
  hasActiveBookingFilters,
} from "@/features/bookings/schemas/booking-query";
import type {
  BookingQuery,
  ResourceLookup,
} from "@/features/bookings/types/booking";
import type { Service } from "@/features/services/types/service";
import { cn } from "@/lib/utils";

export function BookingFilters({
  query,
  services,
  resources,
}: {
  query: BookingQuery;
  services: Service[];
  resources: ResourceLookup[];
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [isPending, startTransition] = useTransition();

  function navigate(overrides: Partial<BookingQuery>) {
    const search = buildBookingSearchParams(query, { page: 0, ...overrides });
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
        "flex flex-col gap-sm rounded-xl border border-border bg-surface p-md xl:flex-row xl:items-center",
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
          aria-label="Search bookings by title"
          placeholder="Search"
          className="h-9 bg-background pl-9 text-body-md"
        />
      </div>

      {/* Filter by service */}
      <NativeSelect
        value={query.serviceId ? String(query.serviceId) : ""}
        onChange={(event) =>
          navigate({
            serviceId: event.target.value
              ? Number(event.target.value)
              : undefined,
          })
        }
        aria-label="Filter by service"
      >
        <option value="">All services</option>
        {services.map((service) => (
          <option key={service.serviceId} value={String(service.serviceId)}>
            {service.title}
          </option>
        ))}
      </NativeSelect>

      {/* Filter by staff */}
      <NativeSelect
        value={query.resourceId ? String(query.resourceId) : ""}
        onChange={(event) =>
          navigate({
            resourceId: event.target.value
              ? Number(event.target.value)
              : undefined,
          })
        }
        aria-label="Filter by staff"
      >
        <option value="">All staff</option>
        {resources.map((resource) => (
          <option key={resource.resourceId} value={String(resource.resourceId)}>
            {resource.name}
          </option>
        ))}
      </NativeSelect>

      <NativeSelect
        value={query.sort ?? ""}
        onChange={(event) =>
          navigate({ sort: event.target.value || undefined })
        }
        aria-label="Sort bookings"
      >
        <option value="">Newest start first</option>
        <option value="startTime,asc">Soonest start first</option>
        <option value="title,asc">Title A-Z</option>
        <option value="status,asc">Status A-Z</option>
        <option value="createdAt,desc">Recently created</option>
      </NativeSelect>

      <button type="submit" className="sr-only">
        Search
      </button>

      {hasActiveBookingFilters(query) ? (
        <button
          type="button"
          onClick={() =>
            navigate({
              q: undefined,
              serviceId: undefined,
              resourceId: undefined,
            })
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
