import { NextResponse, type NextRequest } from "next/server";

import { servicePayloadSchema } from "@/features/services/schemas/service-schema";
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

  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json(
      { message: "Invalid request body.", status: 400 },
      { status: 400 }
    );
  }

  const payload = servicePayloadSchema.safeParse(body);
  if (!payload.success) {
    return NextResponse.json(
      { message: "Missing or invalid service fields.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const result = await withAccessToken((accessToken) =>
      createServiceWithPolicyOnPlatform(accessToken, payload.data)
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
