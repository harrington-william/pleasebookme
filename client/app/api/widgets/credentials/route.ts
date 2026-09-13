import { NextResponse } from "next/server";

import { generateWidgetCredentialsOnPlatform } from "@/features/widgets/services/widget-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor } from "@/lib/session";

/**
 * The one route whose response body carries a plaintext secret. Nothing is
 * persisted by this call — the pair only becomes real when the widget is saved
 * — so the body is forwarded as-is and must never be logged here.
 */
export async function POST() {
  const actor = await getSessionActor();
  if (!actor) {
    return NextResponse.json(
      { message: "Session expired. Please sign in again.", status: 401 },
      { status: 401 }
    );
  }

  try {
    const credentials = await withAccessToken((accessToken) =>
      generateWidgetCredentialsOnPlatform(accessToken)
    );

    return NextResponse.json(credentials);
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
