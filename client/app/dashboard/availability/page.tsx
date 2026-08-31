import { Plus } from "lucide-react";
import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { AvailabilityRulesetList } from "@/features/availability/components/availability-ruleset-list";
import { listMyAvailabilityRulesetsOnPlatform } from "@/features/availability/services/availability-gateway";
import type { AvailabilityRuleset } from "@/features/availability/types/availability";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Availability",
  description:
    "Manage your working hours and scheduling configuration.",
};

export default async function AvailabilityPage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let rulesets: AvailabilityRuleset[] = [];
  let loadError: string | null = null;

  try {
    rulesets = await withAccessToken(
      (accessToken) =>
        listMyAvailabilityRulesetsOnPlatform(accessToken, actor.subject),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError =
      "Could not load your availability rulesets. Please refresh to try again.";
  }

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-xl p-md md:p-2xl">

      {/* Title */}
      <div className="flex flex-col justify-between gap-md sm:flex-row sm:items-end">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Availability
          </h1>

          <p className="text-body-md text-muted-foreground">
            Manage your working hours and scheudling configuration.
          </p>
        </div>

        {/* Create Availability Button */}
        <Link
          href="/dashboard/availability/new"
          className="
            inline-flex w-auto justify-center items-center gap-xs
            rounded-lg bg-primary px-md py-sm text-label-md
            text-primary-foreground transition-colors hover:bg-primary/90
          "
        >
          <Plus className="size-4" aria-hidden="true" />
          Create Availability
        </Link>
      </div>

      {loadError ? (
        <p className="text-body-md text-destructive">{loadError}</p>
      ) : (
        <AvailabilityRulesetList rulesets={rulesets} />
      )}

    </main>
  );
}
