import axios from "axios";

import type { ServicePayload } from "@/features/services/schemas/service-schema";
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

export class ServiceNotFoundError extends Error {
  constructor() {
    super("Service not found.");
    this.name = "ServiceNotFoundError";
  }
}

/**
 * The platform upserts the nested booking policy alongside the service on both
 * POST and PUT, so one request covers the whole editor. PUT is full-replace:
 * every field the form owns has to be present or it is written as null.
 */
function toServiceRequest(input: ServicePayload): ServiceRequest {
  return {
    title: input.title,
    slug: input.slug,
    description: input.description,
    location: input.location,
    scheduleId: input.scheduleId,
    timezone: input.timezone,
    minPrice: input.price,
    maxPrice: input.price,
    currency: input.currency,
    disableCancelling: input.disableCancelling,
    successRedirectUrl: input.successRedirectUrl,
    maxActiveBookingPerBooker: input.maxActiveBookingPerBooker,
    bookingPolicy: {
      defaultDuration: input.defaultDuration,
      beforeBuffer: input.beforeBuffer,
      afterBuffer: input.afterBuffer,
      minimumNotice: input.minimumNotice,
      maximumAdvanceBooking: input.maximumAdvanceBooking,
      bookingWindowType: input.bookingWindowType,
      capacity: input.capacity,
      // core.services.requires_confirmation duplicates this column and is being
      // dropped; the platform mirrors it from here, so only auto_confirm is sent.
      autoConfirm: input.requiresConfirmation,
    },
  };
}

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

export async function updateServiceOnPlatform(
  accessToken: string,
  serviceId: number,
  request: ServiceRequest
): Promise<Service> {
  const response = await platformClient().put<Service>(
    `${SERVICE_BASE}/${serviceId}`,
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

export async function getServiceCatalogEntryOnPlatform(
  accessToken: string,
  serviceId: number
): Promise<ServiceCatalogEntry> {
  try {
    const response = await platformClient().get<Service>(
      `${SERVICE_BASE}/${serviceId}`,
      { headers: bearer(accessToken) }
    );

    const service = response.data;
    return { service, bookingPolicy: service.bookingPolicy ?? null };
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new ServiceNotFoundError();
    }
    throw error;
  }
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

  // GET /services maps through the single-argument ServiceResponse.from, which
  // leaves bookingPolicy null on every row — the policy has to be fetched per
  // service until the list endpoint returns it.
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
  input: ServicePayload
): Promise<ServiceCatalogEntry> {
  const service = await createServiceOnPlatform(
    accessToken,
    toServiceRequest(input)
  );

  return { service, bookingPolicy: service.bookingPolicy ?? null };
}

export async function updateServiceWithPolicyOnPlatform(
  accessToken: string,
  serviceId: number,
  input: ServicePayload
): Promise<ServiceCatalogEntry> {
  try {
    const service = await updateServiceOnPlatform(
      accessToken,
      serviceId,
      toServiceRequest(input)
    );

    return { service, bookingPolicy: service.bookingPolicy ?? null };
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new ServiceNotFoundError();
    }
    throw error;
  }
}
