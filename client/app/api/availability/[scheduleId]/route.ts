import { NextResponse, type NextRequest } from "next/server";

import { deleteAvailabilityRulesetOnPlatform } from "@/features/availability/services/availability-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

export async function DELETE(
  _request: NextRequest,
  context: { params: Promise<{ scheduleId: string }> }
) {
  const { scheduleId } = await context.params;
  const parsed = Number(scheduleId);

  if (!scheduleId || !Number.isInteger(parsed)) {
    return NextResponse.json(
      { message: "Missing or invalid ruleset identifier.", status: 400 },
      { status: 400 }
    );
  }

  try {
    await withAccessToken((accessToken) =>
      deleteAvailabilityRulesetOnPlatform(accessToken, parsed)
    );

    return new NextResponse(null, { status: 204 });
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
