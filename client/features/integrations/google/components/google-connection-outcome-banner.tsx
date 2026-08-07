"use client";

import { CircleCheck, CircleAlert, Info, TriangleAlert } from "lucide-react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

import {
  isGoogleConnectOutcome,
  type GoogleConnectOutcome,
} from "@/features/integrations/google/types/google-connection";
import { cn } from "@/lib/utils";

/**
 * Reports the result of a Google consent round trip.
 *
 * The `?google=<outcome>` query parameter is the ONLY signal the frontend gets
 * about what happened — the entire callback is handled by Spring Boot, which
 * then 302s the browser back here. There is no Next.js callback route, and no
 * authorization code or token ever reaches this code.
 *
 * Two things happen on mount:
 *  - the parameter is stripped from the URL, so a refresh or a shared link does
 *    not re-show a stale result;
 *  - on `connected`, the route is refreshed, because the Server Component that
 *    rendered the connection list ran BEFORE this redirect landed and therefore
 *    does not yet include the new connection.
 */

type Severity = "success" | "neutral" | "warning" | "destructive";

const OUTCOMES: Record<
  GoogleConnectOutcome,
  { severity: Severity; message: string }
> = {
  connected: {
    severity: "success",
    message: "Google account connected.",
  },
  denied: {
    // A user declining consent is a choice, not a failure. Styling this as an
    // error would imply something went wrong when nothing did.
    severity: "neutral",
    message: "You declined the Google consent screen — nothing was changed.",
  },
  invalid_state: {
    severity: "warning",
    message:
      "This connection attempt expired or was already used. Please try again.",
  },
  missing_code: {
    severity: "warning",
    message: "Google did not return the expected response. Please try again.",
  },
  error: {
    severity: "destructive",
    message: "Something went wrong connecting your Google account.",
  },
};

const SEVERITY_STYLES: Record<Severity, string> = {
  // Per DESIGN.md "Chips/Badges": a ~10% tint of the semantic colour with the
  // solid colour for text, keeping the surface quiet.
  success: "border-success/30 bg-success/10 text-success",
  neutral: "border-border bg-surface-container text-muted-foreground",
  warning: "border-warning/30 bg-warning/10 text-warning",
  destructive: "border-destructive/30 bg-destructive/10 text-destructive",
};

const SEVERITY_ICONS: Record<Severity, typeof CircleCheck> = {
  success: CircleCheck,
  neutral: Info,
  warning: TriangleAlert,
  destructive: CircleAlert,
};

export function GoogleConnectionOutcomeBanner() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const raw = searchParams.get("google");

  // Captured once, at mount, via a lazy initializer rather than written from
  // the effect below. The effect immediately strips `?google=` from the URL, so
  // `raw` goes null on the very next render — the outcome has to be remembered
  // independently of the query string. A lazy initializer is the right tool
  // here: the user always arrives on this page through a full page load (Spring
  // Boot 302s the browser back), so the component is guaranteed to mount fresh.
  const [outcome] = useState<GoogleConnectOutcome | null>(() =>
    isGoogleConnectOutcome(raw) ? raw : null
  );

  useEffect(() => {
    if (!isGoogleConnectOutcome(raw)) return;

    // Drop the parameter so refreshing does not resurrect the banner.
    const next = new URLSearchParams(searchParams.toString());
    next.delete("google");
    const query = next.toString();
    router.replace(query ? `${pathname}?${query}` : pathname, {
      scroll: false,
    });

    if (raw === "connected") {
      // The list was fetched server-side before this redirect arrived.
      router.refresh();
    }
  }, [raw, pathname, router, searchParams]);

  if (!outcome) return null;

  const { severity, message } = OUTCOMES[outcome];
  const Icon = SEVERITY_ICONS[severity];

  return (
    <div
      role="status"
      className={cn(
        "flex items-start gap-sm rounded-lg border px-md py-sm text-body-md",
        SEVERITY_STYLES[severity]
      )}
    >
      <Icon className="mt-[2px] size-4 shrink-0" aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}
