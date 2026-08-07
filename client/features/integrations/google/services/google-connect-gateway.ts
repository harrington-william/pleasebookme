import type {
  GoogleConnectRequest,
  GoogleConnectResponse,
  OAuthConnectionSummary,
} from "@/features/integrations/google/types/google-connection";
import { bearer, platformClient } from "@/lib/axios";

/**
 * Server-side gateway to the platform's Google integration endpoints.
 *
 * SERVER ONLY — reads API_BASE_URL and must never be imported by a Client
 * Component.
 *
 * Unlike auth-gateway.ts, every call here is Bearer-authenticated: these are
 * the first non-permitAll platform endpoints this client talks to. Callers must
 * obtain the token through `withAccessToken` (lib/authenticated-platform-request)
 * rather than reading the cookie directly, so a 15-minute-expired access token
 * transparently refreshes instead of failing.
 *
 * Errors propagate as axios errors; the route handler or page normalizes them.
 */

const GOOGLE_BASE = "/api/v1/integrations/google";

/**
 * POST /connect → { authorizationUrl }
 *
 * Writes nothing to Google. It generates PKCE + a single-use `state`, stores
 * them in Redis for 10 minutes, and returns the consent URL for the BROWSER to
 * navigate to. Deliberately not a redirect — see google-connect-api.ts.
 */
export async function initiateGoogleConnectOnPlatform(
  accessToken: string,
  request: GoogleConnectRequest
): Promise<GoogleConnectResponse> {
  const response = await platformClient().post<GoogleConnectResponse>(
    `${GOOGLE_BASE}/connect`,
    request,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

/**
 * GET /connections → the caller's own connections.
 *
 * Owner-scoped by the server's query itself, not by a filter we pass.
 */
export async function listGoogleConnectionsOnPlatform(
  accessToken: string
): Promise<OAuthConnectionSummary[]> {
  const response = await platformClient().get<OAuthConnectionSummary[]>(
    `${GOOGLE_BASE}/connections`,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

/**
 * DELETE /connections/{uid} → 204
 *
 * Revokes at Google first, then marks the local row REVOKED. The server returns
 * the same "not found" error for someone else's connection as for a
 * non-existent one, so this endpoint cannot be used to probe which UIDs exist.
 */
export async function disconnectGoogleConnectionOnPlatform(
  accessToken: string,
  oauthConnectionUid: string
): Promise<void> {
  await platformClient().delete(
    `${GOOGLE_BASE}/connections/${encodeURIComponent(oauthConnectionUid)}`,
    { headers: bearer(accessToken) }
  );
}
