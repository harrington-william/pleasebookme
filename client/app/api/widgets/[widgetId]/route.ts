import { NextResponse, type NextRequest } from "next/server";

import { widgetUpdatePayloadSchema } from "@/features/widgets/schemas/widget-schema";
import {
  deleteWidgetOnPlatform,
  updateWidgetOnPlatform,
  WidgetNotFoundError,
} from "@/features/widgets/services/widget-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor } from "@/lib/session";

function parseWidgetId(widgetId: string): number | null {
  const parsed = Number(widgetId);
  return widgetId && Number.isInteger(parsed) ? parsed : null;
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ widgetId: string }> }
) {
  const { widgetId } = await context.params;
  const parsed = parseWidgetId(widgetId);

  if (parsed === null) {
    return NextResponse.json(
      { message: "Missing or invalid widget identifier.", status: 400 },
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

  const payload = widgetUpdatePayloadSchema.safeParse(body);
  if (!payload.success) {
    return NextResponse.json(
      { message: "Missing or invalid widget fields.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const widget = await withAccessToken((accessToken) =>
      updateWidgetOnPlatform(accessToken, parsed, payload.data)
    );

    return NextResponse.json(widget);
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      return NextResponse.json(
        { message: error.message, status: 401 },
        { status: 401 }
      );
    }

    if (error instanceof WidgetNotFoundError) {
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
  context: { params: Promise<{ widgetId: string }> }
) {
  const { widgetId } = await context.params;
  const parsed = parseWidgetId(widgetId);

  if (parsed === null) {
    return NextResponse.json(
      { message: "Missing or invalid widget identifier.", status: 400 },
      { status: 400 }
    );
  }

  try {
    await withAccessToken((accessToken) =>
      deleteWidgetOnPlatform(accessToken, parsed)
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
