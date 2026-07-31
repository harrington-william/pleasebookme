import type {
  AuthTokens,
  LoginRequest,
  RegisterRequest,
} from "@/features/auth/types/auth";
import { platformClient } from "@/lib/axios";

/**
 * Server-side gateway to the platform's auth domain.
 *
 * SERVER ONLY — this reads API_BASE_URL and must never be imported by a Client
 * Component. The browser reaches these operations through the BFF route
 * handlers in app/api/auth/*, which are the only callers of this module.
 *
 * Every function returns the platform's response verbatim and lets axios errors
 * propagate; translating them into something renderable is the route handler's
 * job (see auth-errors.ts), so the gateway stays a thin, faithful transport.
 */

/** All platform auth endpoints live under this prefix and are permitAll(). */
const AUTH_BASE = "/api/v1/auth";

/** POST /api/v1/auth/register → 201 { accessToken, refreshToken } */
export async function registerOnPlatform(
  payload: RegisterRequest
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/register`,
    payload
  );
  return response.data;
}

/** POST /api/v1/auth/login → 200 { accessToken, refreshToken } */
export async function loginOnPlatform(
  payload: LoginRequest
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/login`,
    payload
  );
  return response.data;
}

/**
 * POST /api/v1/auth/refresh → 200 { accessToken, refreshToken }
 *
 * The platform rotates the pair, so the caller must persist BOTH returned
 * tokens, not just the access token.
 */
export async function refreshOnPlatform(
  refreshToken: string
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/refresh`,
    { refreshToken }
  );
  return response.data;
}
