/**
 * Wire contract for Google delegated authorization.
 *
 * Answers "may the platform act on this user's behalf against Google Calendar,
 * Sheets and Drive". This is NOT Google Sign-In ("who is this user"), which
 * lives in features/auth. They share no frontend code — see SECURITY.md §11
 * vs §12 for the same distinction on the server.
 *
 * Types mirror the server DTOs 1:1; drift is a bug.
 */

/** Source: server/.../security/oauth/google/authorization/GoogleScope.java */
export const GOOGLE_SCOPES = ["CALENDAR", "SHEETS", "DRIVE_FILE"] as const;
export type GoogleScope = (typeof GOOGLE_SCOPES)[number];

/**
 * Granted-scope URIs, as Google echoes them back.
 *
 * The server always appends `openid`, `email` and `profile` to every
 * authorization request (GoogleScope.baseScopes), so these appear in responses
 * too even though they are not selectable.
 */
export const GOOGLE_SCOPE_URIS: Record<GoogleScope, string> = {
  CALENDAR: "https://www.googleapis.com/auth/calendar",
  SHEETS: "https://www.googleapis.com/auth/spreadsheets",
  DRIVE_FILE: "https://www.googleapis.com/auth/drive.file",
};

/** Source: server/.../service/integration/dto/GoogleConnectRequest.java */
export interface GoogleConnectRequest {
  /** Omitted or empty means "all scopes" on the server, not "none". */
  scopes?: GoogleScope[];
  redirectAfter?: string;
}

/** Source: server/.../service/integration/dto/GoogleConnectResponse.java */
export interface GoogleConnectResponse {
  authorizationUrl: string;
}

/** Source: server/.../integration/enums/OAuthProvider.java */
export type OAuthProvider = "GOOGLE";

/** Source: server/.../integration/enums/OAuthConnectionStatus.java */
export const OAUTH_CONNECTION_STATUSES = [
  "ACTIVE",
  "REVOKED",
  "EXPIRED",
  "ERROR",
] as const;
export type OAuthConnectionStatus = (typeof OAUTH_CONNECTION_STATUSES)[number];

/**
 * Source: server/.../service/integration/dto/OAuthConnectionSummaryResponse.java
 *
 * NOTE ON `oauthConnectionId`: GOOGLE_OAUTH_FLOW.md §2 predicted this would
 * serialize as a JSON string. It does not. The server registers no custom
 * Jackson number handling (no ObjectMapper bean, no `jackson:` block in
 * application.yaml), so Spring Boot's defaults apply and a `BigInteger`
 * serializes as a JSON **number**. Typed accordingly.
 *
 * Nothing in this feature reads it — every operation is keyed by
 * `oauthConnectionUid`, which is what the DELETE endpoint takes. It is mirrored
 * only to keep the contract faithful.
 */
export interface OAuthConnectionSummary {
  oauthConnectionId: number;
  /** UUID. The external identifier used by DELETE /connections/{uid}. */
  oauthConnectionUid: string;
  provider: OAuthProvider;
  providerEmail: string;
  /** Raw granted scope URIs from Google — NOT GoogleScope enum names. */
  scopes: string[];
  status: OAuthConnectionStatus;
  /** ISO instant. */
  tokenExpiresAt: string;
  connectedAt: string;
  lastRefreshedAt: string;
  lastUsedAt: string | null;
}

/**
 * The five outcomes DefaultGoogleConnectService.complete() can redirect with,
 * delivered as `?google=<outcome>` on the page the user lands back on.
 *
 * This query parameter is the ONLY signal the frontend receives about what
 * happened. No authorization code, state value or Google token ever reaches
 * frontend code at any point in this flow.
 */
export const GOOGLE_CONNECT_OUTCOMES = [
  "connected",
  "denied",
  "invalid_state",
  "missing_code",
  "error",
] as const;
export type GoogleConnectOutcome = (typeof GOOGLE_CONNECT_OUTCOMES)[number];

export function isGoogleConnectOutcome(
  value: string | null | undefined
): value is GoogleConnectOutcome {
  return (
    typeof value === "string" &&
    (GOOGLE_CONNECT_OUTCOMES as readonly string[]).includes(value)
  );
}

/**
 * Human label for a granted scope URI — the inverse of the server's
 * `GoogleScope.uri()`, plus the three base scopes the server always requests.
 */
const SCOPE_LABELS: Record<string, string> = {
  [GOOGLE_SCOPE_URIS.CALENDAR]: "Calendar",
  [GOOGLE_SCOPE_URIS.SHEETS]: "Sheets",
  [GOOGLE_SCOPE_URIS.DRIVE_FILE]: "Drive (app files)",
  openid: "Sign-in",
  email: "Email address",
  profile: "Basic profile",
};

export function labelForScopeUri(uri: string): string {
  if (SCOPE_LABELS[uri]) return SCOPE_LABELS[uri];

  // Unknown scope: show the trailing segment rather than a wall of URL.
  const trailing = uri.split("/").pop();
  return trailing && trailing.length > 0 ? trailing : uri;
}

/** Label shown on the connect control for each selectable scope. */
export const GOOGLE_SCOPE_LABELS: Record<GoogleScope, string> = {
  CALENDAR: "Google Calendar",
  SHEETS: "Google Sheets",
  DRIVE_FILE: "Google Drive (files this app creates)",
};
