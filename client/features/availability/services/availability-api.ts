import type {
  AvailabilityRuleset,
  CreateAvailabilityRulesetInput,
} from "@/features/availability/types/availability";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

export async function createAvailabilityRuleset(
  input: CreateAvailabilityRulesetInput
): Promise<AvailabilityRuleset> {
  try {
    const response = await bffClient.post<AvailabilityRuleset>(
      "/availability",
      input
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function updateAvailabilityRuleset(
  scheduleId: number,
  input: CreateAvailabilityRulesetInput
): Promise<AvailabilityRuleset> {
  try {
    const response = await bffClient.put<AvailabilityRuleset>(
      `/availability/${scheduleId}`,
      input
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function deleteAvailabilityRuleset(
  scheduleId: number
): Promise<void> {
  try {
    await bffClient.delete(`/availability/${scheduleId}`);
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
