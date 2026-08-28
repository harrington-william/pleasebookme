import type { CreateServicePayload } from "@/features/services/schemas/service-schema";
import type { ServiceCatalogEntry } from "@/features/services/types/service";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

export async function createService(
  input: CreateServicePayload
): Promise<ServiceCatalogEntry> {
  try {
    const response = await bffClient.post<ServiceCatalogEntry>(
      "/services",
      input
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function deleteService(serviceId: number): Promise<void> {
  try {
    await bffClient.delete(`/services/${serviceId}`);
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
