import type {
  CreateResourceFormValues,
  CreateResourceTypeFormValues,
} from "@/features/resources/schemas/resource-schema";
import type {
  Resource,
  ResourceRequest,
  ResourceType,
  ResourceTypeRequest,
} from "@/features/resources/types/resource";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

function toResourceRequest(values: CreateResourceFormValues): ResourceRequest {
  return {
    resourceTypeId: Number(values.resourceTypeId),
    name: values.name,
    slug: values.slug,
    description: values.description || undefined,
    capacity: values.capacity ? Number(values.capacity) : undefined,
    status: values.status,
  };
}

function toResourceTypeRequest(
  values: CreateResourceTypeFormValues
): ResourceTypeRequest {
  return {
    name: values.name,
    description: values.description || undefined,
    icon: values.icon || undefined,
  };
}

export async function createResource(
  values: CreateResourceFormValues
): Promise<Resource> {
  try {
    const response = await bffClient.post<Resource>(
      "/resources",
      toResourceRequest(values)
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function createResourceType(
  values: CreateResourceTypeFormValues
): Promise<ResourceType> {
  try {
    const response = await bffClient.post<ResourceType>(
      "/resource-types",
      toResourceTypeRequest(values)
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
