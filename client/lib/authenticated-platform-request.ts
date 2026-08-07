import axios from "axios";

import { refreshOnPlatform } from "@/features/auth/services/auth-gateway";
import {
  createSession,
  destroySession,
  getAccessToken,
  getRefreshToken,
} from "@/lib/session";

/**
 * Bearer-authenticated calls to the platform, with one-shot refresh and retry.
 *
 * SERVER ONLY — reads httpOnly cookies via next/headers.
 *
 * WHY THIS EXISTS: the access-token cookie lives 15 minutes, but a session is
 * renewable for 30 days. Nothing stops a user from sitting on a page for 20
 * minutes and then clicking a button. A naive Bearer call would fail with 401
 * even though the session is perfectly valid, so every authenticated platform
 * call routes through here instead.
 *
 * The retry is deliberately ONE attempt. If a refresh succeeds and the retried
 * call still returns 401/403, the problem is authorization, not staleness, and
 * retrying again would just loop.
 *
 * COOKIE-WRITE CONSTRAINT: `createSession`/`destroySession` call `cookies().set()`,
 * which Next.js only permits inside a Route Handler or Server Function — never
 * during a Server Component render. So a Server Component that calls this may
 * read fine on a fresh token but will throw when a refresh is actually needed.
 * `allowSessionWrite: false` opts out of persisting the rotated pair for those
 * callers; see the note on SessionExpiredError below.
 */

/**
 * Thrown when there is no usable session and refreshing cannot fix it. Callers
 * should redirect to /login (Server Components) or surface a 401 (Route
 * Handlers) rather than retrying.
 */
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
  /**
   * Whether the rotated token pair may be written back to cookies. Must be
   * false when called during a Server Component render, where Next.js forbids
   * cookie writes.
   */
  allowSessionWrite?: boolean;
};

/**
 * Runs `call` with a valid platform access token.
 *
 * On a 401/403 from the platform, refreshes once and retries. Throws
 * SessionExpiredError when there is no access token, no refresh token, the
 * refresh itself fails, or the retry still fails to authenticate.
 */
export async function withAccessToken<T>(
  call: (accessToken: string) => Promise<T>,
  { allowSessionWrite = true }: WithAccessTokenOptions = {}
): Promise<T> {
  const accessToken = await getAccessToken();
  const refreshToken = await getRefreshToken();

  // No refresh token means no session at all — nothing to attempt.
  if (!refreshToken) {
    throw new SessionExpiredError("No active session.");
  }

  if (accessToken) {
    try {
      return await call(accessToken);
    } catch (error) {
      if (!isAuthFailure(error)) throw error;
      // Fall through to refresh-and-retry.
    }
  }

  let rotated;
  try {
    rotated = await refreshOnPlatform(refreshToken);
  } catch {
    // The platform maps expired/revoked refresh tokens to 403. Either way the
    // session is unrecoverable, so clear it rather than leaving the browser
    // holding a token that can never succeed again.
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
