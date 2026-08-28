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

/**
 * TEMPORARY WORKAROUND — same shape as availability-gateway.ts.
 *
 * GET /api/v1/services and GET /api/v1/booking-policies are generic CRUD
 * endpoints with no owner/organization scoping at all — they return every
 * service and every booking policy for every organization on the platform.
 * This function fetches everything and filters down to the signed-in user's
 * own organization server-side, so the browser only ever receives the
 * filtered subset. The underlying gap — any authenticated caller's token can
 * already list every other organization's services directly against the
 * platform — is a backend authorization gap this client cannot close.
 */
export async function listMyServiceCatalogOnPlatform(
  accessToken: string,
  userUid: string
): Promise<ServiceCatalogEntry[]> {
  const { organizationId } = await resolveCurrentPlatformUser(
    accessToken,
    userUid
  );

  const [servicesResponse, policiesResponse] = await Promise.all([
    platformClient().get<Service[]>(SERVICE_BASE, {
      headers: bearer(accessToken),
    }),
    platformClient().get<BookingPolicy[]>(BOOKING_POLICY_BASE, {
      headers: bearer(accessToken),
    }),
  ]);

  const myServices = servicesResponse.data.filter(
    (service) => service.organizationId === organizationId
  );
  const policyByServiceId = new Map<number, BookingPolicy>(
    policiesResponse.data.map((policy) => [policy.serviceId, policy])
  );

  return myServices
    .map((service) => ({
      service,
      bookingPolicy: policyByServiceId.get(service.serviceId) ?? null,
    }))
    .sort((a, b) => b.service.serviceId - a.service.serviceId);
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
