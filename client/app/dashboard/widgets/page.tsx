import { Plus } from "lucide-react";
import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { WidgetFilters } from "@/features/widgets/components/widget-filters";
import { WidgetList } from "@/features/widgets/components/widget-list";
import { WidgetPagination } from "@/features/widgets/components/widget-pagination";
import { WidgetStatsTiles } from "@/features/widgets/components/widget-stats";
import {
  hasActiveWidgetFilters,
  parseWidgetQuery,
  type WidgetSearchParams,
} from "@/features/widgets/schemas/widget-query";
import { listMyWidgetsOnPlatform } from "@/features/widgets/services/widget-gateway";
import type { WidgetListResult } from "@/features/widgets/types/widget";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

const WIDGETS_PATH = "/dashboard/widgets";

export const metadata: Metadata = {
  title: "Widgets",
  description: "Manage widgets deployed across your websites and channels.",
};

export default async function WidgetsPage({
  searchParams,
}: {
  searchParams: Promise<WidgetSearchParams>;
}) {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  const query = parseWidgetQuery(await searchParams);

  let result: WidgetListResult | null = null;
  let loadError: string | null = null;

  try {
    result = await withAccessToken(
      (accessToken) =>
        listMyWidgetsOnPlatform(accessToken, actor.subject, query),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError = "Could not load your widgets. Please refresh to try again.";
  }

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-lg p-md md:p-2xl">

      {/* Title */}
      <div className="flex flex-col justify-between gap-md sm:flex-row sm:items-end">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Widgets
          </h1>
          <p className="text-body-md text-muted-foreground">
            Manage widgets deployed across your websites and channels.
          </p>
        </div>

        <Link
          href={`${WIDGETS_PATH}/new`}
          className="inline-flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90"
        >
          <Plus className="size-4" aria-hidden="true" />
          Create Widget
        </Link>
      </div>

      {loadError || !result ? (
        <p className="text-body-md text-destructive">{loadError}</p>
      ) : (
        <>
          <WidgetStatsTiles stats={result.stats} />

          <WidgetFilters
            query={query}
            totalElements={result.page.totalElements}
          />

          <WidgetList
            widgets={result.page.content}
            filtered={hasActiveWidgetFilters(query)}
            clearHref={WIDGETS_PATH}
          />

          <WidgetPagination query={query} page={result.page} />
        </>
      )}
    </main>
  );
}
