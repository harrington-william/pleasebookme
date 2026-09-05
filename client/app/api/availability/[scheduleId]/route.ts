import { NextResponse, type NextRequest } from "next/server";

import {
  deleteAvailabilityRulesetOnPlatform,
  ScheduleNotFoundError,
  updateAvailabilityRulesetOnPlatform,
} from "@/features/availability/services/availability-gateway";
import type { AvailabilityWindow } from "@/features/availability/types/availability";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor } from "@/lib/session";

function parseScheduleId(scheduleId: string): number | null {
  const parsed = Number(scheduleId);
  return scheduleId && Number.isInteger(parsed) ? parsed : null;
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ scheduleId: string }> }
) {
  const { scheduleId } = await context.params;
  const parsed = parseScheduleId(scheduleId);

  if (parsed === null) {
    return NextResponse.json(
      { message: "Missing or invalid ruleset identifier.", status: 400 },
      { status: 400 }
    );
  }

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
      updateAvailabilityRulesetOnPlatform(accessToken, actor.subject, parsed, {
        title: body.title as string,
        timezone: body.timezone as string,
        windows: body.windows as AvailabilityWindow[],
      })
    );

    return NextResponse.json(result);
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      return NextResponse.json(
        { message: error.message, status: 401 },
        { status: 401 }
      );
    }

    if (error instanceof ScheduleNotFoundError) {
      return NextResponse.json(
        { message: error.message, status: 404 },
        { status: 404 }
      );
    }

    const normalized = normalizeApiError(error);
    return NextResponse.json(normalized, { status: normalized.status || 502 });
  }
}

export async function DELETE(
  _request: NextRequest,
  context: { params: Promise<{ scheduleId: string }> }
) {
  const { scheduleId } = await context.params;
  const parsed = parseScheduleId(scheduleId);

  if (parsed === null) {
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
