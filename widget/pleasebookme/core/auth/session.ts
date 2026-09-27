import axios from "axios";
import { ApiRequestError, normalizeApiError } from "../api/errors";

export function createSession({ apiUrl, publicKey, secretKey }: {
  apiUrl: string; publicKey: string; secretKey: string;
}) {
  // Widget access tokens belong only to this provider's memory, never persistent storage.
  let token: string | null = null;
  let inFlight: Promise<string> | null = null;
  let generation = 0;

  function bootstrap(): Promise<string> {
    if (inFlight) return inFlight;
    const currentGeneration = generation;
    const pending = axios.post<{ accessToken: string }>(
      `${apiUrl.replace(/\/$/, "")}/api/v1/auth/widget/bootstrap`,
      { publicKey, secretKey, origin: window.location.origin },
      { timeout: 20_000, withCredentials: false, headers: { "Content-Type": "application/json" } }
    ).then(({ data }) => {
      if (currentGeneration === generation) token = data.accessToken;
      return data.accessToken;
    }).catch((error: unknown) => {
      throw new ApiRequestError(normalizeApiError(error));
    }).finally(() => {
      if (inFlight === pending) inFlight = null;
    });
    inFlight = pending;
    return pending;
  }

  return {
    getToken: () => token ? Promise.resolve(token) : bootstrap(),
    refresh: () => {
      // Concurrent expired requests share one bootstrap instead of minting a token each.
      token = null;
      return bootstrap();
    },
    invalidate: () => { token = null; generation += 1; inFlight = null; },
  };
}
