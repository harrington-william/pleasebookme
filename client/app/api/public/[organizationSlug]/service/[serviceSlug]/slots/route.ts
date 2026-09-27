import { NextResponse, type NextRequest } from "next/server";
import { z } from "zod";

import {
  fetchPublicSlots,
} from "@/features/public-booking/services/public-booking-gateway";
import {
  PublicPageNotFoundError,
  RateLimitedError,
} from "@/features/public-booking/services/public-booking-errors";
import { normalizeApiError } from "@/lib/api-error";

const dateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/);

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
  const parsed = dateSchema.safeParse(request.nextUrl.searchParams.get("date"));
  if (!parsed.success) {
    return NextResponse.json(
      { message: "Missing or invalid date.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const slots = await fetchPublicSlots(
      organizationSlug,
      serviceSlug,
      parsed.data,
      forwardedFor(request)
    );
    return NextResponse.json(slots);
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
