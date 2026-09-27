import { NextResponse, type NextRequest } from "next/server";

import {
  fetchPublicService,
} from "@/features/public-booking/services/public-booking-gateway";
import {
  PublicPageNotFoundError,
  RateLimitedError,
} from "@/features/public-booking/services/public-booking-errors";
import { normalizeApiError } from "@/lib/api-error";

function forwardedFor(request: NextRequest): Record<string, string> {
  const value = request.headers.get("x-forwarded-for");
  return value ? { "X-Forwarded-For": value } : {};
}

export async function GET(
  request: NextRequest,
  context: {
    params: Promise<{ organizationSlug: string; serviceSlug: string }>;
  }
) {
  const { organizationSlug, serviceSlug } = await context.params;

  try {
    const service = await fetchPublicService(
      organizationSlug,
      serviceSlug,
      forwardedFor(request)
    );
    return NextResponse.json(service);
  } catch (error) {
    if (error instanceof PublicPageNotFoundError) {
      return NextResponse.json(
        { message: "This booking page is not available.", status: 404 },
        { status: 404 }
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
