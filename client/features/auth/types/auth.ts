/**
 * Wire contract with the Spring Boot platform's auth domain.
 *
 * These types mirror the server's DTOs one-to-one and must not drift. The
 * server is the authoritative definition; when it changes, this file follows.
 *
 * Source: server/src/main/java/com/pleasebookme/server/service/auth/dto/*.java
 * Endpoints are mounted under /api/v1/auth and are the only permitAll() paths
 * in SecurityConfig.
 */

/** public.locale — the only two values the platform enum accepts. */
export const LOCALES = ["en", "vi"] as const;
export type Locale = (typeof LOCALES)[number];

/** POST /api/v1/auth/register → 201 */
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  /** Optional on the server; omitted entirely rather than sent as null. */
  phone?: string;
  name: string;
  locale?: Locale;
  /** Server defaults to "Australia/Sydney" when omitted. */
  timezone?: string;
}

/**
 * POST /api/v1/auth/login → 200
 *
 * NOTE: the platform authenticates by USERNAME, not email. There is no
 * email-based login path on the server today.
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Shared shape of LoginResponse and RefreshResponse. */
export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export type LoginResponse = AuthTokens;
export type RefreshResponse = AuthTokens;

/** POST /api/v1/auth/refresh → 200 */
export interface RefreshRequest {
  refreshToken: string;
}

/**
 * POST /api/v1/auth/google → 200, returns LoginResponse.
 *
 * Source: server/.../service/auth/dto/GoogleSignInRequest.java
 *
 * This is Google SIGN-IN (authentication) — "who is this user". It is a
 * different concern from Google delegated authorization ("may we act on this
 * user's Google Calendar"), which lives in features/integrations/google.
 * The two share nothing on the frontend; do not merge them.
 *
 * `idToken` is the JWT credential minted by Google Identity Services in the
 * browser. The platform verifies its signature, issuer and audience, then
 * discards it — it is never stored.
 */
export interface GoogleSignInRequest {
  idToken: string;
}

/**
 * The platform's ApiErrorResponse shape, produced by GlobalExceptionHandler
 * for every *typed* domain exception.
 *
 * WARNING: this is not the only error shape the server can emit. Bean
 * validation failures (MethodArgumentNotValidException) and Spring Security
 * authentication failures (BadCredentialsException) have NO registered
 * @ExceptionHandler and therefore fall through to Spring Boot's *default*
 * error envelope instead. See features/auth/services/auth-errors.ts.
 */
export interface PlatformErrorResponse {
  status: string;
  message: string;
  data: unknown;
  /** Caller's remote address, despite the name. Not a client identifier. */
  client: string;
  timestamp: string;
  path: string;
}

/** Actor kinds the platform can authenticate. Only USER is issued to this app. */
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

/**
 * Decoded (NOT verified) JWT payload, as emitted by DefaultJwtGenerator.
 *
 * Claim names are the server's exact wire names. `tenant` is nullable by
 * design: per SECURITY.md a registered user has no Tenant until their
 * organization subscribes to a plan, so null means "no active plan", not an
 * error. Never treat a null tenant as a broken session.
 */
export interface PlatformJwtClaims {
  /** Subject — the actor's UUID. */
  sub: string;
  /** JWT id — the token's own UUID. */
  jti: string;
  iss: string;
  aud: string | string[];
  /** Seconds since epoch. */
  iat: number;
  /** Seconds since epoch. */
  exp: number;
  tenant: string | null;
  actor_type: ActorType;
  token_type: TokenType;
}

/**
 * What the app knows about the current actor without a /me endpoint.
 *
 * The platform exposes no "current user" route yet, so this is derived purely
 * from the access token's claims. It deliberately carries identity/scoping
 * only — no display name, email or roles, because the token does not carry
 * them. Populate this properly once GET /api/v1/auth/me exists.
 */
export interface SessionActor {
  subject: string;
  actorType: ActorType;
  tenantUid: string | null;
  expiresAt: number;
}

/** Normalized, renderable error produced by the client's error mapper. */
export interface AuthError {
  message: string;
  /** HTTP status that produced it, or 0 for network/transport failures. */
  status: number;
  /** Field-scoped messages, when the failure was a validation error. */
  fieldErrors?: Record<string, string>;
}
