import { Plus } from "lucide-react";
import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { ResourceFilters } from "@/features/resources/components/resource-filters";
import { ResourcePagination } from "@/features/resources/components/resource-pagination";
import { ResourceStatsTiles } from "@/features/resources/components/resource-stats";
import { ResourceTable } from "@/features/resources/components/resource-table";
import {
  hasActiveResourceFilters,
  parseResourceQuery,
  type ResourceSearchParams,
} from "@/features/resources/schemas/resource-query";
import { listMyResourcesOnPlatform } from "@/features/resources/services/resource-gateway";
import type { ResourceListResult } from "@/features/resources/types/resource";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Resources",
  description: "Manage reservable assets and their service assignments.",
};

export default async function ResourcesPage({
  searchParams,
}: {
  searchParams: Promise<ResourceSearchParams>;
}) {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  const query = parseResourceQuery(await searchParams);

  let result: ResourceListResult | null = null;
  let loadError: string | null = null;

  try {
    result = await withAccessToken(
      (accessToken) =>
        listMyResourcesOnPlatform(accessToken, actor.subject, query),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError = "Could not load your resources. Please refresh to try again.";
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-lg p-md md:p-2xl">
      <div className="flex flex-col justify-between gap-md sm:flex-row sm:items-end">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Resources
          </h1>
          <p className="text-body-md text-muted-foreground">
            Manage the rooms, equipment, people, and other assets assigned to
            your services.
          </p>
        </div>

        <Link
          href="/dashboard/resources/new"
          className="inline-flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90"
        >
          <Plus className="size-4" aria-hidden="true" />
          Create Resource
        </Link>
      </div>

      {loadError || !result ? (
        <p className="text-body-md text-destructive">{loadError}</p>
      ) : (
        <>
          <ResourceStatsTiles stats={result.stats} />

          <ResourceFilters query={query} resourceTypes={result.resourceTypes} />

          <ResourceTable
            entries={result.entries}
            filtered={hasActiveResourceFilters(query)}
          />

          <ResourcePagination query={query} page={result.page} />
        </>
      )}
    </main>
  );
}
