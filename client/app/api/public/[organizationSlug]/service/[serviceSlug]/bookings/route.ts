import { NextResponse, type NextRequest } from "next/server";

import { bookingRequestSchema } from "@/features/public-booking/schemas/booking-details-schema";
import {
  createPublicBookingOnPlatform,
} from "@/features/public-booking/services/public-booking-gateway";
import {
  PublicPageNotFoundError,
  RateLimitedError,
  SlotUnavailableError,
} from "@/features/public-booking/services/public-booking-errors";
import { normalizeApiError } from "@/lib/api-error";

function forwardedFor(request: NextRequest): Record<string, string> {
  const value = request.headers.get("x-forwarded-for");
  return value ? { "X-Forwarded-For": value } : {};
}

export async function POST(
  request: NextRequest,
  context: {
    params: Promise<{ organizationSlug: string; serviceSlug: string }>;
  }
) {
  const { organizationSlug, serviceSlug } = await context.params;
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json(
      { message: "Invalid request body.", status: 400 },
      { status: 400 }
    );
  }

  const parsed = bookingRequestSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json(
      { message: "Missing or invalid booking details.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const booking = await createPublicBookingOnPlatform(
      organizationSlug,
      serviceSlug,
      parsed.data,
      forwardedFor(request)
    );
    return NextResponse.json(booking, { status: 201 });
  } catch (error) {
    if (error instanceof PublicPageNotFoundError) {
      return NextResponse.json(
        { message: "This booking page is not available.", status: 404 },
        { status: 404 }
      );
    }
    if (error instanceof SlotUnavailableError) {
      return NextResponse.json(
        { message: "That time is no longer available", status: 409 },
        { status: 409 }
      );
    }
    if (error instanceof RateLimitedError) {
      return NextResponse.json(
        { message: error.message, status: 429 },
        {
          status: 429,
          headers: { "Retry-After": String(error.retryAfterSeconds) },
        }
      );
    }
    const normalized = normalizeApiError(error);
    return NextResponse.json(normalized, { status: normalized.status || 502 });
  }
}
