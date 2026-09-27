import type {
  AvailableSlots,
  PublicBooking,
  PublicBookingRequest,
  PublicService,
} from "@/features/public-booking/types/public-booking";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

export async function getService(
  organizationSlug: string,
  serviceSlug: string
): Promise<PublicService> {
  try {
    const response = await bffClient.get<PublicService>(
      `/public/${organizationSlug}/service/${serviceSlug}`
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function getSlots(
  organizationSlug: string,
  serviceSlug: string,
  date: string
): Promise<AvailableSlots> {
  try {
    const response = await bffClient.get<AvailableSlots>(
      `/public/${organizationSlug}/service/${serviceSlug}/slots`,
      { params: { date } }
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function createBooking(
  organizationSlug: string,
  serviceSlug: string,
  request: PublicBookingRequest
): Promise<PublicBooking> {
  try {
    const response = await bffClient.post<PublicBooking>(
      `/public/${organizationSlug}/service/${serviceSlug}/bookings`,
      request
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
