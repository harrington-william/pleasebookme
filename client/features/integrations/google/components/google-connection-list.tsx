import { DisconnectGoogleConnectionButton } from "@/features/integrations/google/components/disconnect-google-connection-button";
import {
  GoogleConnectionStatusBadge,
  GoogleScopeBadge,
} from "@/features/integrations/google/components/google-scope-badge";
import type { OAuthConnectionSummary } from "@/features/integrations/google/types/google-connection";

function formatTimestamp(value: string | null): string {
  if (!value) return "Never";

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return "Unknown";

  return parsed.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function GoogleConnectionList({
  connections,
}: {
  connections: OAuthConnectionSummary[];
}) {
  if (connections.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-border px-md py-lg text-center">
        <p className="text-body-md text-muted-foreground">
          No Google account connected yet.
        </p>
      </div>
    );
  }

  return (
    <ul className="divide-y divide-border rounded-lg border border-border">
      {connections.map((connection) => (
        <li
          key={connection.oauthConnectionUid}
          className="flex flex-col gap-md p-md md:flex-row md:items-start md:justify-between"
        >
          <div className="min-w-0 space-y-sm">
            <div className="flex flex-wrap items-center gap-sm">
              <span className="text-body-lg text-foreground">
                {connection.providerEmail}
              </span>
              <GoogleConnectionStatusBadge status={connection.status} />
            </div>

            <div className="flex flex-wrap gap-xs">
              {connection.scopes.length > 0 ? (
                connection.scopes.map((uri) => (
                  <GoogleScopeBadge key={uri} uri={uri} />
                ))
              ) : (
                <span className="text-label-md text-muted-foreground">
                  No scopes recorded.
                </span>
              )}
            </div>

            <dl className="flex flex-wrap gap-x-lg gap-y-base text-label-md text-muted-foreground">
              <div className="flex gap-xs">
                <dt>Connected</dt>
                <dd className="text-foreground">
                  {formatTimestamp(connection.connectedAt)}
                </dd>
              </div>
              <div className="flex gap-xs">
                <dt>Last used</dt>
                <dd className="text-foreground">
                  {formatTimestamp(connection.lastUsedAt)}
                </dd>
              </div>
            </dl>
          </div>

          {/* A revoked connection has nothing left to revoke — offering the
              action again would just produce an error. */}
          {connection.status === "ACTIVE" ? (
            <DisconnectGoogleConnectionButton
              oauthConnectionUid={connection.oauthConnectionUid}
              providerEmail={connection.providerEmail}
            />
          ) : null}
        </li>
      ))}
    </ul>
  );
}
