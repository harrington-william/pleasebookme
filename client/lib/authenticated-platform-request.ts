import axios from "axios";

import { refreshOnPlatform } from "@/features/auth/services/auth-gateway";
import {
  createSession,
  destroySession,
  getAccessToken,
  getRefreshToken,
} from "@/lib/session";

export class SessionExpiredError extends Error {
  constructor(message = "Session expired.") {
    super(message);
    this.name = "SessionExpiredError";
  }
}

function isAuthFailure(error: unknown): boolean {
  return (
    axios.isAxiosError(error) &&
    (error.response?.status === 401 || error.response?.status === 403)
  );
}

type WithAccessTokenOptions = {
  allowSessionWrite?: boolean;
};

export async function withAccessToken<T>(
  call: (accessToken: string) => Promise<T>,
  { allowSessionWrite = true }: WithAccessTokenOptions = {}
): Promise<T> {
  const accessToken = await getAccessToken();
  const refreshToken = await getRefreshToken();

  // No refresh token means no session at all
  if (!refreshToken) {
    throw new SessionExpiredError("No active session.");
  }

  if (accessToken) {
    try {
      return await call(accessToken);
    } catch (error) {
      if (!isAuthFailure(error)) throw error;
    }
  }

  let rotated;
  try {
    rotated = await refreshOnPlatform(refreshToken);
  } catch {
    if (allowSessionWrite) {
      await destroySession();
    }
    throw new SessionExpiredError("Session expired. Please sign in again.");
  }

  if (allowSessionWrite) {
    await createSession(rotated);
  }

  try {
    return await call(rotated.accessToken);
  } catch (error) {
    if (isAuthFailure(error)) {
      throw new SessionExpiredError("Session expired. Please sign in again.");
    }
    throw error;
  }
}
