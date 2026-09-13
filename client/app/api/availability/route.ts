import { NextResponse, type NextRequest } from "next/server";

import { createAvailabilityRulesetOnPlatform } from "@/features/availability/services/availability-gateway";
import type { AvailabilityWindow } from "@/features/availability/types/availability";
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

  let body: { title?: unknown; timezone?: unknown; windows?: unknown };

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
    typeof body.timezone !== "string" ||
    !Array.isArray(body.windows)
  ) {
    return NextResponse.json(
      {
        message: "Missing ruleset name, timezone, or weekly schedule.",
        status: 400,
      },
      { status: 400 }
    );
  }

  try {
    const result = await withAccessToken((accessToken) =>
      createAvailabilityRulesetOnPlatform(accessToken, actor.subject, {
        title: body.title as string,
        timezone: body.timezone as string,
        windows: body.windows as AvailabilityWindow[],
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
