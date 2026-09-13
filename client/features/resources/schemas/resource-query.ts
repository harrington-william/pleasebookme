import {
  RESOURCE_SORT_FIELDS,
  RESOURCE_STATUSES,
  type ResourceQuery,
  type ResourceSortField,
  type ResourceStatus,
} from "@/features/resources/types/resource";

export const DEFAULT_PAGE_SIZE = 20;

export type ResourceSearchParams = Record<string, string | string[] | undefined>;

function first(value: string | string[] | undefined): string | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const trimmed = raw?.trim();
  return trimmed ? trimmed : undefined;
}

function parseStatus(value: string | undefined): ResourceStatus | undefined {
  return RESOURCE_STATUSES.includes(value as ResourceStatus)
    ? (value as ResourceStatus)
    : undefined;
}

/**
 * The platform re-validates and clamps all of these; parsing here only keeps a
 * hand-edited URL from producing a request the server would reject.
 */
function parseSort(value: string | undefined): string | undefined {
  if (!value) return undefined;

  const [field, direction] = value.split(",");
  if (!RESOURCE_SORT_FIELDS.includes(field as ResourceSortField)) {
    return undefined;
  }

  return direction === "asc" || direction === "desc"
    ? `${field},${direction}`
    : field;
}

function parsePositiveInteger(value: string | undefined): number | undefined {
  if (!value) return undefined;

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
}

export function parseResourceQuery(
  searchParams: ResourceSearchParams
): ResourceQuery {
  const page = Number(first(searchParams.page));

  return {
    page: Number.isInteger(page) && page >= 0 ? page : 0,
    size: DEFAULT_PAGE_SIZE,
    resourceTypeId: parsePositiveInteger(first(searchParams.resourceTypeId)),
    status: parseStatus(first(searchParams.status)),
    q: first(searchParams.q),
    sort: parseSort(first(searchParams.sort)),
  };
}

export function buildResourceSearchParams(
  query: ResourceQuery,
  overrides: Partial<ResourceQuery> = {}
): string {
  const merged = { ...query, ...overrides };
  const params = new URLSearchParams();

  if (merged.q) params.set("q", merged.q);
  if (merged.resourceTypeId) {
    params.set("resourceTypeId", String(merged.resourceTypeId));
  }
  if (merged.status) params.set("status", merged.status);
  if (merged.sort) params.set("sort", merged.sort);
  if (merged.page > 0) params.set("page", String(merged.page));

  const serialized = params.toString();
  return serialized ? `?${serialized}` : "";
}

export function hasActiveResourceFilters(query: ResourceQuery): boolean {
  return Boolean(query.q || query.resourceTypeId || query.status);
}
