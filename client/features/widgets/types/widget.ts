export const WIDGET_STATUSES = [
  "REGISTERING",
  "ACTIVE",
  "DISABLED",
  "REVOKED",
] as const;

export type WidgetStatus = (typeof WIDGET_STATUSES)[number];

export const WIDGET_TYPES = ["INLINE", "POPUP", "FULL_PAGE", "EMBEDDED"] as const;

export type WidgetType = (typeof WIDGET_TYPES)[number];

/**
 * The platform accepts all four types; this client ships one. Everything that
 * offers a type to the user — the picker, the filter — reads from this list,
 * so widening it later is a one-line change.
 */
export const SUPPORTED_WIDGET_TYPES = ["INLINE"] as const satisfies readonly WidgetType[];

export const WIDGET_TYPE_LABELS: Record<WidgetType, string> = {
  INLINE: "Inline",
  POPUP: "Popup",
  FULL_PAGE: "Full page",
  EMBEDDED: "Embedded",
};

/** The two statuses a client may write; REVOKED is DELETE-only, REGISTERING is never written. */
export const WIDGET_EDITABLE_STATUSES = ["ACTIVE", "DISABLED"] as const satisfies readonly WidgetStatus[];

export type WidgetEditableStatus = (typeof WIDGET_EDITABLE_STATUSES)[number];

// GET /api/v1/widgets/{widgetId}, and every item of the list
export interface Widget {
  widgetId: number;
  widgetUid: string;
  tenantId: number;
  name: string;
  status: WidgetStatus;
  type: WidgetType;
  originValidation: boolean;
  publicKey: string;
  origin: string | null;
  issuedAt: string;
  expiresAt: string | null;
  lastUsedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

// POST /api/v1/widgets/credentials — the only response that carries a secret
export interface WidgetCredentials {
  publicKey: string;
  secretKey: string;
}

// POST /api/v1/widgets
export interface WidgetCreateRequest {
  name: string;
  type?: WidgetType;
  origin?: string | null;
  credentials: WidgetCredentials;
}

// PUT /api/v1/widgets/{widgetId}
export interface WidgetUpdateRequest {
  name: string;
  type?: WidgetType;
  status?: WidgetEditableStatus;
  origin?: string | null;
  credentials?: WidgetCredentials | null;
}

// GET /api/v1/widgets
export interface WidgetPage {
  content: Widget[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface WidgetPageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// GET /api/v1/widgets/stats
export interface WidgetStats {
  total: number;
  active: number;
  disabled: number;
  revoked: number;
}

export const WIDGET_SORT_FIELDS = [
  "name",
  "status",
  "type",
  "createdAt",
  "updatedAt",
] as const;

export type WidgetSortField = (typeof WIDGET_SORT_FIELDS)[number];

export interface WidgetQuery {
  page: number;
  size: number;
  type?: WidgetType;
  status?: WidgetEditableStatus;
  sort?: string;
}

export interface WidgetListResult {
  page: WidgetPage;
  stats: WidgetStats;
}
