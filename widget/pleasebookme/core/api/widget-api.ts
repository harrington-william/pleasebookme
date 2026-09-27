import type { AxiosInstance } from "axios";
import type { BookingApi, Organization, Service, AvailableSlots, Booking } from "../../ecosystems/barbershop/logic/types";
import { ApiRequestError, normalizeApiError } from "./errors";

export function createWidgetApi(client: AxiosInstance): BookingApi {
  async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
    try { return (await operation()).data; }
    catch (error) { throw new ApiRequestError(normalizeApiError(error)); }
  }
  const servicePath = (slug: string) => `/api/v1/widget/services/${encodeURIComponent(slug)}`;
  return {
    getOrganization: () => request(() => client.get<Organization>("/api/v1/widget/organization")),
    getService: (slug) => request(() => client.get<Service>(servicePath(slug))),
    getSlots: (slug, date) => request(() => client.get<AvailableSlots>(`${servicePath(slug)}/slots`, { params: { date } })),
    createBooking: (slug, body) => request(() => client.post<Booking>(`${servicePath(slug)}/bookings`, body)),
  };
}
