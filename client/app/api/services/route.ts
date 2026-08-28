import { NextResponse, type NextRequest } from "next/server";

import { createServiceWithPolicyOnPlatform } from "@/features/services/services/service-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor } from "@/lib/session";

export async function POST(request: NextRequest) {
  const actor = await getSessionActor();
  if (!actor) {
    return NextResponse.json(
      { message: "Session expired. Please sign in again.", status: 401 },
      { status: 401 }
    );
  }

  let body: Record<string, unknown>;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json(
      { message: "Invalid request body.", status: 400 },
      { status: 400 }
    );
  }

  if (
    typeof body.title !== "string" ||
    typeof body.slug !== "string" ||
    typeof body.scheduleId !== "number" ||
    typeof body.defaultDuration !== "number" ||
    typeof body.beforeBuffer !== "number" ||
    typeof body.afterBuffer !== "number" ||
    typeof body.minimumNotice !== "number" ||
    typeof body.maximumAdvanceBooking !== "number" ||
    typeof body.capacity !== "number" ||
    typeof body.bookingWindowType !== "string"
  ) {
    return NextResponse.json(
      { message: "Missing or invalid service fields.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const result = await withAccessToken((accessToken) =>
      createServiceWithPolicyOnPlatform(accessToken, {
        title: body.title as string,
        slug: body.slug as string,
        description: body.description as string | undefined,
        scheduleId: body.scheduleId as number,
        price: body.price as number | undefined,
        defaultDuration: body.defaultDuration as number,
        beforeBuffer: body.beforeBuffer as number,
        afterBuffer: body.afterBuffer as number,
        minimumNotice: body.minimumNotice as number,
        maximumAdvanceBooking: body.maximumAdvanceBooking as number,
        capacity: body.capacity as number,
        bookingWindowType: body.bookingWindowType as string,
      })
    );

    return NextResponse.json(result, { status: 201 });
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
