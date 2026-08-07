import type { OAuthConnectionStatus } from "@/features/integrations/google/types/google-connection";
import { labelForScopeUri } from "@/features/integrations/google/types/google-connection";
import { cn } from "@/lib/utils";

/**
 * Small chips for granted scopes and connection status.
 *
 * Per DESIGN.md "Chips/Badges": 4px radius, a ~10% tint of the semantic colour
 * with the solid colour for text. Deliberately quiet — a settings page listing
 * several of these should not look like a dashboard of alarms.
 */

export function GoogleScopeBadge({ uri }: { uri: string }) {
  return (
    <span className="inline-flex items-center rounded-sm border border-border bg-surface-container px-xs py-[2px] font-mono text-mono-label text-muted-foreground">
      {labelForScopeUri(uri)}
    </span>
  );
}

const STATUS_STYLES: Record<OAuthConnectionStatus, string> = {
  ACTIVE: "border-success/30 bg-success/10 text-success",
  REVOKED: "border-border bg-surface-container text-muted-foreground",
  EXPIRED: "border-warning/30 bg-warning/10 text-warning",
  ERROR: "border-destructive/30 bg-destructive/10 text-destructive",
};

const STATUS_LABELS: Record<OAuthConnectionStatus, string> = {
  ACTIVE: "Active",
  REVOKED: "Revoked",
  EXPIRED: "Expired",
  ERROR: "Error",
};

export function GoogleConnectionStatusBadge({
  status,
}: {
  status: OAuthConnectionStatus;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-[6px] rounded-sm border px-xs py-[2px] text-label-md",
        STATUS_STYLES[status]
      )}
    >
      {/* 6px solid dot, per DESIGN.md "Status Indicators". No pulse: none of
          these states are "active processing". */}
      <span
        aria-hidden="true"
        className="size-[6px] rounded-full bg-current"
      />
      {STATUS_LABELS[status]}
    </span>
  );
}
