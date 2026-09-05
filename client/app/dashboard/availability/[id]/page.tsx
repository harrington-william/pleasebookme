import type { Metadata } from "next";
import Link from "next/link";
import { notFound, redirect } from "next/navigation";

import { AvailabilityRulesetForm } from "@/features/availability/components/availability-ruleset-form";
import { toEditAvailabilityFormValues } from "@/features/availability/schemas/availability-schema";
import {
  getAvailabilityRulesetOnPlatform,
  ScheduleNotFoundError,
} from "@/features/availability/services/availability-gateway";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Edit Availability",
  description:
    "Update scheduling rules and working hours for this ruleset.",
};

export default async function AvailabilityRulesetPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const scheduleId = Number(id);

  if (!id || !Number.isInteger(scheduleId)) {
    notFound();
  }

  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let defaultValues;

  try {
    const ruleset = await withAccessToken(
      (accessToken) =>
        getAvailabilityRulesetOnPlatform(accessToken, actor.subject, scheduleId),
      { allowSessionWrite: false }
    );
    defaultValues = toEditAvailabilityFormValues(ruleset);
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    if (error instanceof ScheduleNotFoundError) {
      notFound();
    }

    throw error;
  }

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-xl p-md md:p-2xl">
      <div className="flex flex-col justify-between gap-md md:flex-row md:items-center">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Edit Availability
          </h1>
          <p className="text-body-md text-muted-foreground">
            Update scheduling rules and working hours for this ruleset.
          </p>
        </div>

        <Link
          href="/dashboard/availability"
          className="w-auto rounded-lg border border-border text-center text-destructive
          px-md py-sm text-label-md transition-colors hover:bg-surface-hover"
        >
          Cancel
        </Link>
      </div>

      <AvailabilityRulesetForm
        mode="edit"
        scheduleId={scheduleId}
        defaultValues={defaultValues}
      />
    </main>
  );
}
