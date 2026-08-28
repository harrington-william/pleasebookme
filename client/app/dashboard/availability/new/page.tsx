import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { CreateAvailabilityForm } from "@/features/availability/components/create-availability-form";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Create Availability",
  description:
    "Configure scheduling rules and working hours for this ruleset.",
};

export default async function CreateAvailabilityPage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-xl p-md md:p-2xl">
      <div className="flex flex-col justify-between gap-md md:flex-row md:items-center">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Create New Availability
          </h1>
          <p className="text-body-md text-muted-foreground">
            Configure scheduling rules and working hours for this ruleset.
          </p>
        </div>

        <Link
          href="/dashboard/availability"
          className="w-auto rounded-lg border border-border text-center px-md py-sm text-label-md text-foreground transition-colors hover:bg-surface-hover"
        >
          Cancel
        </Link>
      </div>

      <CreateAvailabilityForm />
    </main>
  );
}
