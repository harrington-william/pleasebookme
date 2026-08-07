import type { Metadata } from "next";
import { redirect } from "next/navigation";
import { Suspense } from "react";

import { ConnectGoogleButton } from "@/features/integrations/google/components/connect-google-button";
import { GoogleConnectionList } from "@/features/integrations/google/components/google-connection-list";
import { GoogleConnectionOutcomeBanner } from "@/features/integrations/google/components/google-connection-outcome-banner";
import { listGoogleConnectionsOnPlatform } from "@/features/integrations/google/services/google-connect-gateway";
import type { OAuthConnectionSummary } from "@/features/integrations/google/types/google-connection";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor } from "@/lib/session";

export const metadata: Metadata = {
  title: "Integrations",
  description: "Connect Google Calendar, Sheets and Drive to your workspace.",
};

/**
 * Google integrations settings.
 *
 * THE URL MATTERS. proxy.ts only session-gates the `/dashboard` prefix, and the
 * server's DEFAULT_REDIRECT_AFTER is "/settings/integrations" — without the
 * /dashboard prefix. Every /connect call from this app therefore passes
 * `redirectAfter: "/dashboard/settings/integrations"` explicitly so the
 * post-consent redirect lands here, inside the protected prefix, on a route
 * that actually exists. See app/api/integrations/google/connect/route.ts.
 *
 * There is no Next.js callback route anywhere in this flow: Google redirects
 * the browser straight to Spring Boot, which handles everything and then 302s
 * back here with `?google=<outcome>`.
 */
export default async function GoogleIntegrationsPage() {
  // proxy.ts already gates /dashboard, but per the Next.js data-security guide
  // route-level interception is not sufficient on its own — re-check here,
  // close to the data.
  const actor = await getSessionActor();
  if (!actor) {
    redirect("/login");
  }

  let connections: OAuthConnectionSummary[] = [];
  let loadError: string | null = null;

  try {
    connections = await withAccessToken(
      (accessToken) => listGoogleConnectionsOnPlatform(accessToken),
      // Cookie writes are illegal during a Server Component render, so a token
      // rotated here cannot be persisted. The refreshed token is still used for
      // this request; the next mutation through a Route Handler will persist a
      // fresh pair properly.
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect("/login");
    }

    // A platform hiccup should not blank the page — the connect action below
    // is still perfectly usable.
    loadError =
      "Could not load your connected accounts. Please refresh to try again.";
  }

  return (
    <main className="mx-auto flex w-full max-w-[720px] flex-col gap-lg p-md md:p-2xl">
      <header className="space-y-xs">
        <p className="text-label-md tracking-wider text-muted-foreground uppercase">
          Settings
        </p>
        <h1 className="text-headline-md text-foreground">Integrations</h1>
        <p className="text-body-md text-muted-foreground">
          Connect a Google account so PleaseBookMe can sync bookings to your
          calendar and export data to your spreadsheets.
        </p>
      </header>

      {/* useSearchParams requires a Suspense boundary during prerendering. */}
      <Suspense fallback={null}>
        <GoogleConnectionOutcomeBanner />
      </Suspense>

      <section className="relative z-10 space-y-md rounded-xl border border-border bg-surface p-lg">
        <h2 className="text-headline-md text-foreground">Connected accounts</h2>

        {loadError ? (
          <p className="text-body-md text-destructive">{loadError}</p>
        ) : (
          <GoogleConnectionList connections={connections} />
        )}
      </section>

      <section className="relative z-10 space-y-md rounded-xl border border-border bg-surface p-lg">
        <h2 className="text-headline-md text-foreground">
          Connect a Google account
        </h2>
        <ConnectGoogleButton />
      </section>
    </main>
  );
}
