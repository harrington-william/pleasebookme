import type { Booking } from "@/features/bookings/types/booking";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

export async function cancelBooking(bookingId: number): Promise<Booking> {
  try {
    const response = await bffClient.post<Booking>(
      `/bookings/${bookingId}/cancel`
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
