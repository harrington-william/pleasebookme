import type {
  BookingPolicy,
  BookingPolicyRequest,
  Service,
  ServiceCatalogEntry,
  ServiceRequest,
} from "@/features/services/types/service";
import { bearer, platformClient } from "@/lib/axios";
import { resolveCurrentPlatformUser } from "@/lib/platform-user";

const SERVICE_BASE = "/api/v1/services";
const BOOKING_POLICY_BASE = "/api/v1/booking-policies";

export async function createServiceOnPlatform(
  accessToken: string,
  request: ServiceRequest
): Promise<Service> {
  const response = await platformClient().post<Service>(
    SERVICE_BASE,
    request,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

export async function deleteServiceOnPlatform(
  accessToken: string,
  serviceId: number
): Promise<void> {
  await platformClient().delete(`${SERVICE_BASE}/${serviceId}`, {
    headers: bearer(accessToken),
  });
}

export async function createBookingPolicyOnPlatform(
  accessToken: string,
  request: BookingPolicyRequest
): Promise<BookingPolicy> {
  const response = await platformClient().post<BookingPolicy>(
    BOOKING_POLICY_BASE,
    request,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

export async function getServicesByOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<Service[]> {
  const response = await platformClient().get<Service[]>(SERVICE_BASE, {
    headers: bearer(accessToken),
    params: { organizationId },
  });
  return response.data;
}

export async function getBookingPolicyForServiceOnPlatform(
  accessToken: string,
  serviceId: number
): Promise<BookingPolicy | null> {
  const response = await platformClient().get<BookingPolicy[]>(
    BOOKING_POLICY_BASE,
    {
      headers: bearer(accessToken),
      params: { serviceId },
    }
  );
  return response.data[0] ?? null;
}

export async function listMyServiceCatalogOnPlatform(
  accessToken: string,
  userUid: string
): Promise<ServiceCatalogEntry[]> {
  const { organizationId } = await resolveCurrentPlatformUser(
    accessToken,
    userUid
  );

  const services = await getServicesByOrganizationOnPlatform(
    accessToken,
    organizationId
  );

  const entries = await Promise.all(
    services.map(async (service) => ({
      service,
      bookingPolicy: await getBookingPolicyForServiceOnPlatform(
        accessToken,
        service.serviceId
      ),
    }))
  );

  return entries.sort((a, b) => b.service.serviceId - a.service.serviceId);
}

export async function createServiceWithPolicyOnPlatform(
  accessToken: string,
  input: {
    title: string;
    slug: string;
    description?: string;
    scheduleId: number;
    price?: number;
    defaultDuration: number;
    beforeBuffer: number;
    afterBuffer: number;
    minimumNotice: number;
    maximumAdvanceBooking: number;
    capacity: number;
    bookingWindowType: string;
  }
): Promise<ServiceCatalogEntry> {
  const service = await createServiceOnPlatform(accessToken, {
    title: input.title,
    slug: input.slug,
    description: input.description,
    scheduleId: input.scheduleId,
    minPrice: input.price,
    maxPrice: input.price,
    bookingPolicy: {
      defaultDuration: input.defaultDuration,
      minimumNotice: input.minimumNotice,
      maximumAdvanceBooking: input.maximumAdvanceBooking,
      beforeBuffer: input.beforeBuffer,
      afterBuffer: input.afterBuffer,
      bookingWindowType: input.bookingWindowType,
      capacity: input.capacity,
    },
  });

  return { service, bookingPolicy: service.bookingPolicy ?? null };
}
