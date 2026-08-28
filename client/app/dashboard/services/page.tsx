import { Plus } from "lucide-react";
import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { ServiceCatalogList } from "@/features/services/components/service-catalog-list";
import { listMyServiceCatalogOnPlatform } from "@/features/services/services/service-gateway";
import type { ServiceCatalogEntry } from "@/features/services/types/service";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Services",
  description:
    "Manage your bookable offerings, configure durations, and assign resources.",
};

export default async function ServicesPage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let entries: ServiceCatalogEntry[] = [];
  let loadError: string | null = null;

  try {
    entries = await withAccessToken(
      (accessToken) => listMyServiceCatalogOnPlatform(accessToken, actor.subject),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError = "Could not load your services. Please refresh to try again.";
  }

  return (
    <main className="mx-auto flex w-full max-w-6xl flex-col gap-lg p-md md:p-2xl">

      {/* Title */}
      <div className="flex flex-col justify-between gap-md sm:flex-row sm:items-end">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Services Catalog
          </h1>
          
          <p className="text-body-md text-muted-foreground">
            Manage your bookable offerings, configure durations, and assign resources.
          </p>
        </div>

        {/* Create Service Button */}
        <Link
          href="/dashboard/services/new"
          className="inline-flex w-auto justify-center items-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90"
        >
          <Plus className="size-4" aria-hidden="true" />
          Create New Service
        </Link>
      </div>

      {loadError ? (
        <p className="text-body-md text-destructive">{loadError}</p>
      ) : (
        <ServiceCatalogList entries={entries} />
      )}
    </main>
  );
}
