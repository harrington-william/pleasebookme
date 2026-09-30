import { NextResponse, type NextRequest } from "next/server";

import { rejectBookingOnPlatform } from "@/features/bookings/services/booking-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

export async function PUT(
  _request: NextRequest,
  context: { params: Promise<{ bookingId: string }> }
) {
  const { bookingId } = await context.params;
  const parsed = Number(bookingId);

  if (!bookingId || !Number.isInteger(parsed)) {
    return NextResponse.json(
      { message: "Missing or invalid booking identifier.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const result = await withAccessToken((accessToken) =>
      rejectBookingOnPlatform(accessToken, parsed)
    );

    return NextResponse.json(result);
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      return NextResponse.json(
        { message: error.message, status: 401 },
        { status: 401 }
      );
    }

    const normalized = normalizeApiError(error);
    return NextResponse.json(normalized, { status: normalized.status || 502 });
  }
}
