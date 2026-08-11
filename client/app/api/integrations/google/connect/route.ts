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

/**
 * POST /api/auth-adjacent BFF boundary: /api/integrations/google/connect
 *
 * Returns `{ authorizationUrl }` for the browser to navigate to. It does NOT
 * redirect: the caller is an XHR carrying no ability to follow a cross-origin
 * redirect to Google's consent screen, so the navigation has to be a deliberate
 * `window.location.assign` client-side.
 */
export async function POST(request: NextRequest) {
  let scopes: GoogleScope[] | undefined;

  try {
    const body = await request.json();

    if (Array.isArray(body?.scopes)) {
      const requested = body.scopes.filter((scope: unknown): scope is GoogleScope =>
        (GOOGLE_SCOPES as readonly string[]).includes(scope as string)
      );
      // An empty array means "all scopes" to the server, which is not what a
      // caller sending an empty selection intends. Send undefined instead only
      // when nothing valid was supplied at all.
      scopes = requested.length > 0 ? requested : undefined;
    }
  } catch {
    // No body is fine — the server treats it as "all scopes".
    scopes = undefined;
  }

  try {
    const result = await withAccessToken((accessToken) =>
      initiateGoogleConnectOnPlatform(accessToken, {
        scopes,
        // ALWAYS explicit. The server's DEFAULT_REDIRECT_AFTER now matches this
        // (it was corrected from "/settings/integrations", which lacked the
        // /dashboard prefix and would have landed outside proxy.ts's protected
        // prefix on a route that does not exist here). Keep sending it anyway:
        // this app's routing is not the server's to know, and the two happening
        // to agree today is not a reason to depend on it.
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
