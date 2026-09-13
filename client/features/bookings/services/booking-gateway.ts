import type {
  Attendee,
  Booking,
  BookingListEntry,
  BookingListResult,
  BookingPage,
  BookingQuery,
  BookingResourceLink,
  OrganizationSummary,
  ResourceLookup,
} from "@/features/bookings/types/booking";
import { getServicesByOrganizationOnPlatform } from "@/features/services/services/service-gateway";
import { bearer, platformClient } from "@/lib/axios";
import { resolveCurrentPlatformUser } from "@/lib/platform-user";

const BOOKING_BASE = "/api/v1/bookings";
const ATTENDEE_BASE = "/api/v1/attendees";
const BOOKING_RESOURCE_BASE = "/api/v1/booking-resources";
const RESOURCE_BASE = "/api/v1/resources";
const ORGANIZATION_BASE = "/api/v1/organizations";

async function getBookingPageOnPlatform(
  accessToken: string,
  organizationId: number,
  query: BookingQuery
): Promise<BookingPage> {
  const response = await platformClient().get<BookingPage>(BOOKING_BASE, {
    headers: bearer(accessToken),
    params: {
      organizationId,
      page: query.page,
      size: query.size,
      tab: query.tab,
      serviceId: query.serviceId,
      resourceId: query.resourceId,
      q: query.q,
      sort: query.sort,
    },
  });

  return response.data;
}

async function getAttendeesByOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<Attendee[]> {
  const response = await platformClient().get<Attendee[]>(ATTENDEE_BASE, {
    headers: bearer(accessToken),
    params: { organizationId },
  });

  return response.data;
}

async function getBookingResourcesByOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<BookingResourceLink[]> {
  const response = await platformClient().get<BookingResourceLink[]>(
    BOOKING_RESOURCE_BASE,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );

  return response.data;
}

async function getResourceLookupByOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<ResourceLookup[]> {
  const response = await platformClient().get<ResourceLookup[]>(
    `${RESOURCE_BASE}/lookup`,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );

  return response.data;
}

async function getOrganizationOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<OrganizationSummary> {
  const response = await platformClient().get<OrganizationSummary>(
    `${ORGANIZATION_BASE}/${organizationId}`,
    { headers: bearer(accessToken) }
  );

  return response.data;
}

export async function cancelBookingOnPlatform(
  accessToken: string,
  bookingId: number
): Promise<Booking> {
  const response = await platformClient().get<Booking>(
    `${BOOKING_BASE}/${bookingId}/cancel`,
    { headers: bearer(accessToken) }
  );

  return response.data;
}

/**
 * Assembles one page of bookings with one batched fetch per lookup surface.
 */
export async function listMyBookingsOnPlatform(
  accessToken: string,
  userUid: string,
  query: BookingQuery
): Promise<BookingListResult> {
  const { organizationId } = await resolveCurrentPlatformUser(
    accessToken,
    userUid
  );

  const [page, services, resourceLookup, links, attendees, organization] =
    await Promise.all([
      getBookingPageOnPlatform(accessToken, organizationId, query),
      getServicesByOrganizationOnPlatform(accessToken, organizationId),
      getResourceLookupByOrganizationOnPlatform(accessToken, organizationId),
      getBookingResourcesByOrganizationOnPlatform(accessToken, organizationId),
      getAttendeesByOrganizationOnPlatform(accessToken, organizationId),
      getOrganizationOnPlatform(accessToken, organizationId),
    ]);

  const servicesById = new Map(
    services.map((service) => [service.serviceId, service])
  );
  const resourcesById = new Map(
    resourceLookup.map((resource) => [resource.resourceId, resource])
  );

  const primaryResourceIdByBooking = new Map<number, number>();
  for (const link of links) {
    if (link.isPrimary) {
      primaryResourceIdByBooking.set(link.bookingId, link.resourceId);
    }
  }

  const attendeesByBooking = new Map<number, Attendee[]>();
  for (const attendee of attendees) {
    const existing = attendeesByBooking.get(attendee.bookingId) ?? [];
    existing.push(attendee);
    attendeesByBooking.set(attendee.bookingId, existing);
  }

  const entries: BookingListEntry[] = page.content.map((booking) => {
    const primaryResourceId = primaryResourceIdByBooking.get(booking.bookingId);
    const bookingAttendees = attendeesByBooking.get(booking.bookingId) ?? [];

    return {
      booking,
      service: servicesById.get(booking.serviceId) ?? null,
      primaryResource:
        primaryResourceId !== undefined
          ? resourcesById.get(primaryResourceId) ?? null
          : null,
      attendees: [...bookingAttendees].sort(
        (first, second) => first.attendeeId - second.attendeeId
      ),
    };
  });

  return {
    entries,
    page: {
      page: page.page,
      size: page.size,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
    },
    services,
    resources: resourceLookup,
    organizationTimezone: organization.timezone,
  };
}
