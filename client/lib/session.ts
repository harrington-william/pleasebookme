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

export async function getSessionActor(): Promise<SessionActor | null> {
  const accessToken = await getAccessToken();

  if (accessToken && !isJwtExpired(accessToken)) {
    const actor = toSessionActor(accessToken);
    if (actor) return actor;
  }

  const refreshToken = await getRefreshToken();
  if (!refreshToken || isJwtExpired(refreshToken)) return null;

  return toSessionActor(refreshToken);
}
