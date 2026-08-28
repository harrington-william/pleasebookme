import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { listMySchedulesOnPlatform } from "@/features/availability/services/availability-gateway";
import type { Schedule } from "@/features/availability/types/availability";
import { CreateServiceForm } from "@/features/services/components/create-service-form";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Create Service",
  description: "Configure details, pricing, and availability rules.",
};

export default async function CreateServicePage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let schedules: Schedule[] = [];

  try {
    schedules = await withAccessToken(
      (accessToken) => listMySchedulesOnPlatform(accessToken, actor.subject),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }
    // Fall through with an empty list — the form renders a clear
    // "create an availability ruleset first" message either way.
  }

  return (
    <main className="mx-auto flex w-full max-w-6xl flex-col gap-lg p-md md:p-2xl">
      <div className="flex flex-col justify-between gap-md md:flex-row md:items-center">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Create New Service
          </h1>
          <p className="text-body-md text-muted-foreground">
            Configure details, pricing, and availability rules.
          </p>
        </div>

        <Link
          href="/dashboard/services"
          className="w-max rounded-lg border border-border px-md py-sm text-label-md text-foreground transition-colors hover:bg-surface-hover"
        >
          Cancel
        </Link>
      </div>

      <CreateServiceForm schedules={schedules} />
    </main>
  );
}
