import type {
  GoogleConnectResponse,
  GoogleScope,
} from "@/features/integrations/google/types/google-connection";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

/**
 * Browser-side Google integration operations.
 *
 * These call our own BFF routes, never the platform directly — same pattern as
 * features/auth/services/auth-api.ts. Failures surface as ApiRequestError with
 * an already-renderable message.
 */

/**
 * Asks the platform for a Google consent URL.
 *
 * IMPORTANT: the returned URL must be reached by a real top-level navigation
 * (`window.location.assign`). Do not fetch it. A `fetch` would attempt to load
 * Google's consent screen as a cross-origin XHR and fail — a failure that looks
 * like a CORS misconfiguration but is really a misuse of the flow. The server
 * returns JSON rather than a 302 for exactly this reason.
 */
export async function startGoogleConnect(
  scopes?: GoogleScope[]
): Promise<GoogleConnectResponse> {
  try {
    const response = await bffClient.post<GoogleConnectResponse>(
      "/integrations/google/connect",
      { scopes }
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

/** Revokes a connection at Google and locally. */
export async function disconnectGoogleConnection(
  oauthConnectionUid: string
): Promise<void> {
  try {
    await bffClient.delete(
      `/integrations/google/connections/${encodeURIComponent(oauthConnectionUid)}`
    );
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
