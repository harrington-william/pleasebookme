import axios from "axios";

import type {
  AvailableSlots,
  PublicBooking,
  PublicBookingRequest,
  PublicOrganization,
  PublicService,
} from "@/features/public-booking/types/public-booking";
import { platformClient } from "@/lib/axios";

import {
  PublicPageNotFoundError,
  RateLimitedError,
  SlotUnavailableError,
} from "./public-booking-errors";

const PUBLIC_BASE = "/api/v1/public";

function rethrowPublicError(error: unknown): never {
  if (axios.isAxiosError(error)) {
    if (error.response?.status === 404) throw new PublicPageNotFoundError();
    if (error.response?.status === 409) throw new SlotUnavailableError();
    if (error.response?.status === 429) {
      throw new RateLimitedError(
        Number(error.response.headers["retry-after"] ?? 60)
      );
    }
  }
  throw error;
}

export async function fetchPublicOrganization(
  organizationSlug: string
): Promise<PublicOrganization> {
  try {
    const response = await platformClient().get<PublicOrganization>(
      `${PUBLIC_BASE}/${organizationSlug}`
    );
    return response.data;
  } catch (error) {
    rethrowPublicError(error);
  }
}

export async function fetchPublicService(
  organizationSlug: string,
  serviceSlug: string,
  headers: Record<string, string> = {}
): Promise<PublicService> {
  try {
    const response = await platformClient().get<PublicService>(
      `${PUBLIC_BASE}/${organizationSlug}/service/${serviceSlug}`,
      { headers }
    );
    return response.data;
  } catch (error) {
    rethrowPublicError(error);
  }
}

export async function fetchPublicSlots(
  organizationSlug: string,
  serviceSlug: string,
  date: string,
  headers: Record<string, string> = {}
): Promise<AvailableSlots> {
  try {
    const response = await platformClient().get<AvailableSlots>(
      `${PUBLIC_BASE}/${organizationSlug}/service/${serviceSlug}/slots`,
      { params: { date }, headers }
    );
    return response.data;
  } catch (error) {
    rethrowPublicError(error);
  }
}

export async function createPublicBookingOnPlatform(
  organizationSlug: string,
  serviceSlug: string,
  request: PublicBookingRequest,
  headers: Record<string, string> = {}
): Promise<PublicBooking> {
  try {
    const response = await platformClient().post<PublicBooking>(
      `${PUBLIC_BASE}/${organizationSlug}/service/${serviceSlug}/bookings`,
      request,
      { headers }
    );
    return response.data;
  } catch (error) {
    rethrowPublicError(error);
  }
}
