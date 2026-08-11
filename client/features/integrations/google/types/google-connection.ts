export const GOOGLE_SCOPES = ["CALENDAR", "SHEETS", "DRIVE_FILE"] as const;
export type GoogleScope = (typeof GOOGLE_SCOPES)[number];

export const GOOGLE_SCOPE_URIS: Record<GoogleScope, string> = {
  CALENDAR: "https://www.googleapis.com/auth/calendar",
  SHEETS: "https://www.googleapis.com/auth/spreadsheets",
  DRIVE_FILE: "https://www.googleapis.com/auth/drive.file",
};

export interface GoogleConnectRequest {
  scopes?: GoogleScope[];
  redirectAfter?: string;
}

export interface GoogleConnectResponse {
  authorizationUrl: string;
}

export type OAuthProvider = "GOOGLE";

export const OAUTH_CONNECTION_STATUSES = [
  "ACTIVE",
  "REVOKED",
  "EXPIRED",
  "ERROR",
] as const;
export type OAuthConnectionStatus = (typeof OAUTH_CONNECTION_STATUSES)[number];
export interface OAuthConnectionSummary {
  oauthConnectionId: number;
  oauthConnectionUid: string;
  provider: OAuthProvider;
  providerEmail: string;
  scopes: string[];
  status: OAuthConnectionStatus;
  tokenExpiresAt: string;
  connectedAt: string;
  lastRefreshedAt: string;
  lastUsedAt: string | null;
}

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

const SCOPE_LABELS: Record<string, string> = {
  [GOOGLE_SCOPE_URIS.CALENDAR]: "Calendar",
  [GOOGLE_SCOPE_URIS.SHEETS]: "Sheets",
  [GOOGLE_SCOPE_URIS.DRIVE_FILE]: "Drive (Selected files)",
  openid: "Sign-in",
  email: "Email address",
  profile: "Basic profile",
};

export function labelForScopeUri(uri: string): string {
  if (SCOPE_LABELS[uri]) return SCOPE_LABELS[uri];

  const trailing = uri.split("/").pop();
  return trailing && trailing.length > 0 ? trailing : uri;
}

export const GOOGLE_SCOPE_LABELS: Record<GoogleScope, string> = {
  CALENDAR: "Google Calendar",
  SHEETS: "Google Sheets",
  DRIVE_FILE: "Google Drive (Selected files)",
};
