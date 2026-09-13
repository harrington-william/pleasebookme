export const RESOURCE_STATUSES = [
  "ACTIVE",
  "INACTIVE",
  "MAINTENANCE",
  "RETIRED",
] as const;

export type ResourceStatus = (typeof RESOURCE_STATUSES)[number];

export interface Resource {
  resourceId: number;
  resourceUid: string;
  organizationId: number;
  resourceTypeId: number;
  name: string;
  slug: string;
  description: string | null;
  capacity: number | null;
  status: ResourceStatus;
  isBookable: boolean;
  isVirtual: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ResourceRequest {
  resourceTypeId: number;
  name: string;
  slug: string;
  description?: string;
  capacity?: number;
  status: ResourceStatus;
  isBookable?: boolean;
  isVirtual?: boolean;
}

export interface ResourcePage {
  content: Resource[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ResourceType {
  resourceTypeId: number;
  organizationId: number;
  name: string;
  description: string | null;
  icon: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ResourceTypeRequest {
  name: string;
  description?: string;
  icon?: string;
}

export interface ResourceServiceLink {
  resourceId: number;
  serviceId: number;
  assignedAt: string;
}

export interface ResourceListEntry {
  resource: Resource;
  resourceTypeName: string;
  assignedServiceNames: string[];
}

export interface ResourceStats {
  total: number;
  active: number;
  inactive: number;
  maintenance: number;
  retired: number;
}

export const RESOURCE_SORT_FIELDS = [
  "name",
  "slug",
  "capacity",
  "status",
  "createdAt",
  "updatedAt",
] as const;

export type ResourceSortField = (typeof RESOURCE_SORT_FIELDS)[number];

export interface ResourceQuery {
  page: number;
  size: number;
  resourceTypeId?: number;
  status?: ResourceStatus;
  q?: string;
  sort?: string;
}

export interface ResourcePageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ResourceListResult {
  entries: ResourceListEntry[];
  page: ResourcePageMeta;
  resourceTypes: ResourceType[];
  stats: ResourceStats;
}
