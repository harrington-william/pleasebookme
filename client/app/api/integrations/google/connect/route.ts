import { NextResponse, type NextRequest } from "next/server";

import { initiateGoogleConnectOnPlatform } from "@/features/integrations/google/services/google-connect-gateway";
import {
  GOOGLE_SCOPES,
  type GoogleScope,
} from "@/features/integrations/google/types/google-connection";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

export async function POST(request: NextRequest) {
  let scopes: GoogleScope[] | undefined;

  // Extract body
  try {
    const body = await request.json();

    if (Array.isArray(body?.scopes)) {
      const requested = body.scopes.filter((scope: unknown): scope is GoogleScope =>
        (GOOGLE_SCOPES as readonly string[]).includes(scope as string)
      );
      // Empty array means "all scopes"
      scopes = requested.length > 0 ? requested : undefined;
    }
  } catch {
    // Server treats an empty array as "all scopes"
    scopes = undefined;
  }

  try {
    const result = await withAccessToken((accessToken) =>
      initiateGoogleConnectOnPlatform(accessToken, {
        scopes,
        redirectAfter: "/dashboard/settings/integrations",
      })
    );

    return NextResponse.json(result, { status: 200 });
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
