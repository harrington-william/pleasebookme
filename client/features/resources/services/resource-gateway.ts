import { getServicesByOrganizationOnPlatform } from "@/features/services/services/service-gateway";
import type {
  Resource,
  ResourceListEntry,
  ResourceListResult,
  ResourcePage,
  ResourceQuery,
  ResourceRequest,
  ResourceServiceLink,
  ResourceStats,
  ResourceType,
  ResourceTypeRequest,
} from "@/features/resources/types/resource";
import { bearer, platformClient } from "@/lib/axios";
import { resolveCurrentPlatformUser } from "@/lib/platform-user";

const RESOURCE_BASE = "/api/v1/resources";
const RESOURCE_TYPE_BASE = "/api/v1/resource-types";
const RESOURCE_SERVICE_BASE = "/api/v1/resource-services";

export async function createResourceOnPlatform(
  accessToken: string,
  request: ResourceRequest
): Promise<Resource> {
  const response = await platformClient().post<Resource>(
    RESOURCE_BASE,
    request,
    { headers: bearer(accessToken) }
  );

  return response.data;
}

export async function deleteResourceOnPlatform(
  accessToken: string,
  resourceId: number
): Promise<void> {
  await platformClient().delete(`${RESOURCE_BASE}/${resourceId}`, {
    headers: bearer(accessToken),
  });
}

export async function createResourceTypeOnPlatform(
  accessToken: string,
  request: ResourceTypeRequest
): Promise<ResourceType> {
  const response = await platformClient().post<ResourceType>(
    RESOURCE_TYPE_BASE,
    request,
    { headers: bearer(accessToken) }
  );

  return response.data;
}

export async function getResourceTypesByOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<ResourceType[]> {
  const response = await platformClient().get<ResourceType[]>(
    RESOURCE_TYPE_BASE,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );

  return response.data;
}

async function getResourcePageOnPlatform(
  accessToken: string,
  organizationId: number,
  query: ResourceQuery
): Promise<ResourcePage> {
  const response = await platformClient().get<ResourcePage>(
    RESOURCE_BASE, {
    headers: bearer(accessToken),
    params: {
      organizationId,
      page: query.page,
      size: query.size,
      resourceTypeId: query.resourceTypeId,
      status: query.status,
      q: query.q,
      sort: query.sort,
    },
  });

  return response.data;
}

async function getResourceStatsOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<ResourceStats> {
  const response = await platformClient().get<ResourceStats>(
    `${RESOURCE_BASE}/stats`,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );

  return response.data;
}

async function getResourceServicesByOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<ResourceServiceLink[]> {
  const response = await platformClient().get<ResourceServiceLink[]>(
    RESOURCE_SERVICE_BASE,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );
  
  return response.data;
}

/**
 * Assembles one page of the resources table.
 *
 * Assignments are fetched for the whole organization in a single call and
 * grouped here, rather than one request per row — the per-row shape was an N+1
 * that scaled with the resource count on every page view.
 */
export async function listMyResourcesOnPlatform(
  accessToken: string,
  userUid: string,
  query: ResourceQuery
): Promise<ResourceListResult> {
  const { organizationId } = await resolveCurrentPlatformUser(
    accessToken,
    userUid
  );

  const [page, resourceTypes, services, links, stats] = await Promise.all([
    getResourcePageOnPlatform(accessToken, organizationId, query),
    getResourceTypesByOrganizationOnPlatform(accessToken, organizationId),
    getServicesByOrganizationOnPlatform(accessToken, organizationId),
    getResourceServicesByOrganizationOnPlatform(accessToken, organizationId),
    getResourceStatsOnPlatform(accessToken, organizationId),
  ]);

  const resourceTypeNames = new Map(
    resourceTypes.map((resourceType) => [
      resourceType.resourceTypeId,
      resourceType.name,
    ])
  );
  const serviceNames = new Map(
    services.map((service) => [service.serviceId, service.title])
  );

  const serviceNamesByResource = new Map<number, string[]>();
  for (const link of links) {
    const existing = serviceNamesByResource.get(link.resourceId) ?? [];
    existing.push(serviceNames.get(link.serviceId) ?? `Service ${link.serviceId}`);
    serviceNamesByResource.set(link.resourceId, existing);
  }

  const entries: ResourceListEntry[] = page.content.map((resource) => ({
    resource,
    resourceTypeName:
      resourceTypeNames.get(resource.resourceTypeId) ?? "Unknown type",
    assignedServiceNames: serviceNamesByResource.get(resource.resourceId) ?? [],
  }));

  return {
    entries,
    page: {
      page: page.page,
      size: page.size,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
    },
    resourceTypes,
    stats,
  };
}

export async function listMyResourceTypesOnPlatform(
  accessToken: string,
  userUid: string
): Promise<ResourceType[]> {
  const { organizationId } = await resolveCurrentPlatformUser(
    accessToken,
    userUid
  );
  return getResourceTypesByOrganizationOnPlatform(accessToken, organizationId);
}
