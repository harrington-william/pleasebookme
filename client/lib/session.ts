import { cookies } from "next/headers";

import type { AuthTokens, SessionActor } from "@/features/auth/types/auth";
import {
  ACCESS_TOKEN_COOKIE,
  ACCESS_TOKEN_MAX_AGE,
  REFRESH_TOKEN_COOKIE,
  REFRESH_TOKEN_MAX_AGE,
} from "@/lib/auth-cookies";
import { serverEnv } from "@/lib/env";
import { decodeJwt } from "@/lib/jwt";

/**
 * Session storage.
 *
 * The platform's JWTs are held in httpOnly cookies written by the BFF route
 * handlers. Browser JavaScript can never read them, which is the whole point
 * of routing auth through Next.js instead of calling Spring Boot directly.
 *
 * The tokens are stored as issued — they are NOT re-encrypted or re-signed.
 * Spring Boot already signed them and is the only party that can verify them;
 * wrapping them in a second Next.js-managed signature would add a key to
 * manage and a failure mode to debug without adding security.
 *
 * Server-only: `cookies()` is unavailable in Client Components, and `.set()`
 * additionally only works inside a Route Handler or Server Function.
 */

export { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from "@/lib/auth-cookies";

function cookieOptions(maxAge: number) {
  return {
    httpOnly: true,
    secure: serverEnv().isProduction,
    sameSite: "lax" as const,
    path: "/",
    maxAge,
  };
}

/** Writes both tokens. Call only from a Route Handler or Server Function. */
export async function createSession(tokens: AuthTokens): Promise<void> {
  const cookieStore = await cookies();

  cookieStore.set(
    ACCESS_TOKEN_COOKIE,
    tokens.accessToken,
    cookieOptions(ACCESS_TOKEN_MAX_AGE)
  );
  cookieStore.set(
    REFRESH_TOKEN_COOKIE,
    tokens.refreshToken,
    cookieOptions(REFRESH_TOKEN_MAX_AGE)
  );
}

/** Clears both tokens. Call only from a Route Handler or Server Function. */
export async function destroySession(): Promise<void> {
  const cookieStore = await cookies();

  cookieStore.delete(ACCESS_TOKEN_COOKIE);
  cookieStore.delete(REFRESH_TOKEN_COOKIE);
}

export async function getAccessToken(): Promise<string | null> {
  const cookieStore = await cookies();
  return cookieStore.get(ACCESS_TOKEN_COOKIE)?.value ?? null;
}

export async function getRefreshToken(): Promise<string | null> {
  const cookieStore = await cookies();
  return cookieStore.get(REFRESH_TOKEN_COOKIE)?.value ?? null;
}

/**
 * Reads the current actor from the access token's claims.
 *
 * OPTIMISTIC ONLY — the signature is not verified here (see lib/jwt.ts). Use
 * this to decide what to render, never to authorize access to data. Any call
 * that actually touches platform data must go through the platform, which
 * verifies the token itself.
 *
 * Returns null when there is no access token or it is unreadable. A null
 * `tenantUid` on a valid session is expected, not an error: per SECURITY.md a
 * user has no Tenant until their organization subscribes to a plan.
 */
export async function getSessionActor(): Promise<SessionActor | null> {
  const accessToken = await getAccessToken();
  if (!accessToken) return null;

  const claims = decodeJwt(accessToken);
  if (!claims?.sub) return null;

  return {
    subject: claims.sub,
    actorType: claims.actor_type,
    tenantUid: claims.tenant ?? null,
    expiresAt: claims.exp,
  };
}
