import type {
  AuthTokens,
  GoogleAuthorizeRequest,
  GoogleAuthorizeResponse,
  GoogleSignInRequest,
  LoginRequest,
  RegisterRequest,
} from "@/features/auth/types/auth";
import { platformClient } from "@/lib/axios";

const AUTH_BASE = "/api/v1/auth";

// POST /api/v1/auth/register
export async function registerOnPlatform(
  payload: RegisterRequest
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/register`,
    payload
  );
  return response.data;
}

// POST /api/v1/auth/login
export async function loginOnPlatform(
  payload: LoginRequest
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/login`,
    payload
  );
  return response.data;
}

// POST /api/v1/auth/google
export async function signInWithGoogleOnPlatform(
  payload: GoogleSignInRequest
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/google`,
    payload
  );
  return response.data;
}

// POST /api/v1/auth/google/authorize
// One-shot registration for new users -> OIDC and Consent Screen
export async function authorizeGoogleOnboardingOnPlatform(
  payload: GoogleAuthorizeRequest = {}
): Promise<GoogleAuthorizeResponse> {
  const response = await platformClient().post<GoogleAuthorizeResponse>(
    `${AUTH_BASE}/google/authorize`,
    payload
  );
  return response.data;
}

// POST /api/v1/auth/google/handoff
export async function exchangeGoogleHandoffOnPlatform(
  code: string
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/google/handoff`,
    { code }
  );
  return response.data;
}

// POST /api/v1/auth/refresh
export async function refreshOnPlatform(
  refreshToken: string
): Promise<AuthTokens> {
  const response = await platformClient().post<AuthTokens>(
    `${AUTH_BASE}/refresh`,
    { refreshToken }
  );
  return response.data;
}
