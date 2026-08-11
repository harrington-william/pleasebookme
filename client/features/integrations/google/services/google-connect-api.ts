import type {
  GoogleConnectResponse,
  GoogleScope,
} from "@/features/integrations/google/types/google-connection";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

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
