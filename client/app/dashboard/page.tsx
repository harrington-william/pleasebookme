import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { DashboardMetricTiles } from "@/features/dashboard/components/dashboard-metrics";
import { RecentBookingsTable } from "@/features/dashboard/components/recent-bookings-table";
import { getDashboardSummaryOnPlatform } from "@/features/dashboard/services/dashboard-gateway";
import type { DashboardSummary } from "@/features/dashboard/types/dashboard";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { resolveCurrentPlatformUser } from "@/lib/platform-user";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Dashboard",
};

function formatDashboardDate(value: string): string {
  const [year, month, day] = value.split("-").map(Number);

  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "full",
    timeZone: "UTC",
  }).format(Date.UTC(year, month - 1, day));
}

export default async function DashboardPage() {
  const actor = await getSessionActor();

  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let summary: DashboardSummary | null = null;
  let loadError: string | null = null;

  try {
    summary = await withAccessToken(
      async (accessToken) => {
        const { organizationId } = await resolveCurrentPlatformUser(
          accessToken,
          actor.subject
        );

        return getDashboardSummaryOnPlatform(accessToken, organizationId);
      },
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError = "Could not load your dashboard. Please refresh to try again.";
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-lg p-md md:p-2xl">
      <div className="space-y-xs">
        <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
          Dashboard
        </h1>
        {summary ? (
          <p className="text-body-md text-muted-foreground">
            {formatDashboardDate(summary.date)}
          </p>
        ) : null}
      </div>

      {loadError || !summary ? (
        <p className="text-body-md text-destructive">{loadError}</p>
      ) : (
        <>
          <DashboardMetricTiles metrics={summary.metrics} />
          <RecentBookingsTable
            bookings={summary.recentBookings}
            timezone={summary.timezone}
          />
        </>
      )}
    </main>
  );
}
