import { NextResponse, type NextRequest } from "next/server";

import { disconnectGoogleConnectionOnPlatform } from "@/features/integrations/google/services/google-connect-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

export async function DELETE(
  _request: NextRequest,
  context: { params: Promise<{ uid: string }> }
) {
  const { uid } = await context.params;

  if (!uid) {
    return NextResponse.json(
      { message: "Missing connection identifier.", status: 400 },
      { status: 400 }
    );
  }

  try {
    await withAccessToken((accessToken) =>
      disconnectGoogleConnectionOnPlatform(accessToken, uid)
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
