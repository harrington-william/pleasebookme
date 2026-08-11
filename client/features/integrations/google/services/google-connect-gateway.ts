import type {
  GoogleConnectRequest,
  GoogleConnectResponse,
  OAuthConnectionSummary,
} from "@/features/integrations/google/types/google-connection";
import { bearer, platformClient } from "@/lib/axios";

const GOOGLE_BASE = "/api/v1/integrations/google";

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

export async function listGoogleConnectionsOnPlatform(
  accessToken: string
): Promise<OAuthConnectionSummary[]> {
  const response = await platformClient().get<OAuthConnectionSummary[]>(
    `${GOOGLE_BASE}/connections`,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

export async function disconnectGoogleConnectionOnPlatform(
  accessToken: string,
  oauthConnectionUid: string
): Promise<void> {
  await platformClient().delete(
    `${GOOGLE_BASE}/connections/${encodeURIComponent(oauthConnectionUid)}`,
    { headers: bearer(accessToken) }
  );
}
