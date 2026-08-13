import { cookies } from "next/headers";

import type { AuthTokens, SessionActor } from "@/features/auth/types/auth";
import {
  ACCESS_TOKEN_COOKIE,
  ACCESS_TOKEN_MAX_AGE,
  REFRESH_TOKEN_COOKIE,
  REFRESH_TOKEN_MAX_AGE,
} from "@/lib/auth-cookies";
import { serverEnv } from "@/lib/env";
import { decodeJwt, isJwtExpired } from "@/lib/jwt";

export {
  ACCESS_TOKEN_COOKIE,
  REFRESH_TOKEN_COOKIE,
  SESSION_EXPIRED_REDIRECT,
} from "@/lib/auth-cookies";

function cookieOptions(maxAge: number) {
  return {
    httpOnly: true,
    secure: serverEnv().isProduction,
    sameSite: "lax" as const,
    path: "/",
    maxAge,
  };
}

// Set access & refresh tokens in cookies
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

// Clear both tokens
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

function toSessionActor(token: string): SessionActor | null {
  const claims = decodeJwt(token);
  if (!claims?.sub) return null;

  return {
    subject: claims.sub,
    actorType: claims.actor_type,
    tenantUid: claims.tenant ?? null,
    expiresAt: claims.exp,
  };
}

/**
 * Who is signed in, or null if nobody is.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * THE ACCESS TOKEN IS NOT THE SESSION SIGNAL. The refresh token is.
 *
 * The access cookie lives 15 minutes; the refresh cookie lives 30 days. So for
 * almost the entire life of a session there is no access cookie at all, and
 * reading its absence as "signed out" is simply wrong — the session is alive
 * and renewable, it just needs a round trip nobody has made yet.
 *
 * This used to return null in exactly that window while proxy.ts (correctly)
 * kept saying "signed in", and the two redirected at each other until Chrome
 * gave up with ERR_TOO_MANY_REDIRECTS. It reproduced on every visit made more
 * than 15 minutes after signing in.
 *
 * lib/authenticated-platform-request.ts already models this correctly — it
 * treats a missing refresh token as fatal and a missing access token as
 * routine. This function now agrees with it, and with proxy.ts.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Both tokens are JWTs carrying the same identity claims (sub / actor_type /
 * tenant — see DefaultJwtGenerator on the platform), differing only in
 * `token_type` and lifetime, so identity is readable from whichever survives.
 *
 * Decoded, never verified — for optimistic render decisions only, exactly as
 * lib/jwt.ts warns. The platform re-verifies on every API call.
 */
export async function getSessionActor(): Promise<SessionActor | null> {
  const accessToken = await getAccessToken();

  if (accessToken && !isJwtExpired(accessToken)) {
    const actor = toSessionActor(accessToken);
    if (actor) return actor;
  }

  // No usable access token is the NORMAL steady state, not a failure. Fall back
  // to the refresh token: it answers both "is there a session" and "whose".
  const refreshToken = await getRefreshToken();
  if (!refreshToken || isJwtExpired(refreshToken)) return null;

  // `expiresAt` is now when the SESSION dies rather than when the access token
  // does — which is what a caller asking about the session actually wants.
  return toSessionActor(refreshToken);
}
