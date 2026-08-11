export const LOCALES = ["en", "vi"] as const;
export type Locale = (typeof LOCALES)[number];

// POST /api/v1/auth/register
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phone?: string;
  name: string;
  locale?: Locale;
  timezone?: string;
}

// POST /api/v1/auth/login
export interface LoginRequest {
  username: string;
  password: string;
}

// Shared by login and refresh
export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export type LoginResponse = AuthTokens;
export type RefreshResponse = AuthTokens;

// POST /api/v1/auth/refresh
export interface RefreshRequest {
  refreshToken: string;
}

// POST /api/v1/auth/google
export interface GoogleSignInRequest {
  idToken: string;
}

// POST /api/v1/auth/google/authorize
export interface GoogleAuthorizeRequest {
  redirectAfter?: string;
}

export interface GoogleAuthorizeResponse {
  authorizationUrl: string;
}

// POST /api/v1/auth/google/handoff
export interface GoogleHandoffRequest {
  code: string;
}

export interface PlatformErrorResponse {
  status: string;
  message: string;
  data: unknown;
  client: string;
  timestamp: string;
  path: string;
}

export const ACTOR_TYPES = [
  "USER",
  "SYSTEM",
  "WIDGET",
  "API_KEY",
  "WEBHOOK",
  "INTEGRATION",
] as const;
export type ActorType = (typeof ACTOR_TYPES)[number];

export type TokenType = "ACCESS" | "REFRESH";

export interface PlatformJwtClaims {
  sub: string;
  jti: string;
  iss: string;
  aud: string | string[];
  iat: number;
  exp: number;
  tenant: string | null;
  actor_type: ActorType;
  token_type: TokenType;
}

export interface SessionActor {
  subject: string;
  actorType: ActorType;
  tenantUid: string | null;
  expiresAt: number;
}

export interface AuthError {
  message: string;
  status: number;
  fieldErrors?: Record<string, string>;
}
