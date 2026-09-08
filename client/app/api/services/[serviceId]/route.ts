import { NextResponse, type NextRequest } from "next/server";

import { servicePayloadSchema } from "@/features/services/schemas/service-schema";
import {
  deleteServiceOnPlatform,
  ServiceNotFoundError,
  updateServiceWithPolicyOnPlatform,
} from "@/features/services/services/service-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor } from "@/lib/session";

function parseServiceId(serviceId: string): number | null {
  const parsed = Number(serviceId);
  return serviceId && Number.isInteger(parsed) ? parsed : null;
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ serviceId: string }> }
) {
  const { serviceId } = await context.params;
  const parsed = parseServiceId(serviceId);

  if (parsed === null) {
    return NextResponse.json(
      { message: "Missing or invalid service identifier.", status: 400 },
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
      updateServiceWithPolicyOnPlatform(accessToken, parsed, payload.data)
    );

    return NextResponse.json(result);
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      return NextResponse.json(
        { message: error.message, status: 401 },
        { status: 401 }
      );
    }

    if (error instanceof ServiceNotFoundError) {
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
  context: { params: Promise<{ serviceId: string }> }
) {
  const { serviceId } = await context.params;
  const parsed = parseServiceId(serviceId);

  if (parsed === null) {
    return NextResponse.json(
      { message: "Missing or invalid service identifier.", status: 400 },
      { status: 400 }
    );
  }

  try {
    await withAccessToken((accessToken) =>
      deleteServiceOnPlatform(accessToken, parsed)
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
