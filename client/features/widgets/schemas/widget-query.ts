import {
  SUPPORTED_WIDGET_TYPES,
  WIDGET_EDITABLE_STATUSES,
  WIDGET_SORT_FIELDS,
  type WidgetEditableStatus,
  type WidgetQuery,
  type WidgetSortField,
  type WidgetType,
} from "@/features/widgets/types/widget";

export const DEFAULT_PAGE_SIZE = 20;

export type WidgetSearchParams = Record<string, string | string[] | undefined>;

function first(value: string | string[] | undefined): string | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  const trimmed = raw?.trim();
  return trimmed ? trimmed : undefined;
}

function parseType(value: string | undefined): WidgetType | undefined {
  return (SUPPORTED_WIDGET_TYPES as readonly string[]).includes(value ?? "")
    ? (value as WidgetType)
    : undefined;
}

function parseStatus(
  value: string | undefined
): WidgetEditableStatus | undefined {
  return (WIDGET_EDITABLE_STATUSES as readonly string[]).includes(value ?? "")
    ? (value as WidgetEditableStatus)
    : undefined;
}

/**
 * The platform re-validates and clamps all of these; parsing here only keeps a
 * hand-edited URL from producing a request the server would reject.
 */
function parseSort(value: string | undefined): string | undefined {
  if (!value) return undefined;

  const [field, direction] = value.split(",");
  if (!WIDGET_SORT_FIELDS.includes(field as WidgetSortField)) {
    return undefined;
  }

  return direction === "asc" || direction === "desc"
    ? `${field},${direction}`
    : field;
}

export function parseWidgetQuery(
  searchParams: WidgetSearchParams
): WidgetQuery {
  const page = Number(first(searchParams.page));

  return {
    page: Number.isInteger(page) && page >= 0 ? page : 0,
    size: DEFAULT_PAGE_SIZE,
    type: parseType(first(searchParams.type)),
    status: parseStatus(first(searchParams.status)),
    sort: parseSort(first(searchParams.sort)),
  };
}

export function buildWidgetSearchParams(
  query: WidgetQuery,
  overrides: Partial<WidgetQuery> = {}
): string {
  const merged = { ...query, ...overrides };
  const params = new URLSearchParams();

  if (merged.type) params.set("type", merged.type);
  if (merged.status) params.set("status", merged.status);
  if (merged.sort) params.set("sort", merged.sort);
  if (merged.page > 0) params.set("page", String(merged.page));

  const serialized = params.toString();
  return serialized ? `?${serialized}` : "";
}

export function hasActiveWidgetFilters(query: WidgetQuery): boolean {
  return Boolean(query.type || query.status);
}
