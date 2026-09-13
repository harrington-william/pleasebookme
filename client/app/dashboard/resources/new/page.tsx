import type { Metadata } from "next";
import Link from "next/link";
import { redirect } from "next/navigation";

import { CreateResourceForm } from "@/features/resources/components/create-resource-form";
import { listMyResourceTypesOnPlatform } from "@/features/resources/services/resource-gateway";
import type { ResourceType } from "@/features/resources/types/resource";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Create Resource",
  description: "Add a reservable asset to your organization.",
};

export default async function CreateResourcePage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let resourceTypes: ResourceType[] = [];
  let loadError: string | null = null;

  try {
    resourceTypes = await withAccessToken(
      (accessToken) =>
        listMyResourceTypesOnPlatform(accessToken, actor.subject),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    loadError =
      "Could not load resource types. Please refresh before creating a resource.";
  }

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-lg p-md md:p-2xl">
      <div className="flex flex-col justify-between gap-md sm:flex-row sm:items-center">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            Create Resource
          </h1>
          <p className="text-body-md text-muted-foreground">
            Add an asset that can be assigned to one or more services.
          </p>
        </div>

        <Link
          href="/dashboard/resources"
          className="rounded-lg border border-border px-md py-sm text-center text-label-md text-muted-foreground transition-colors hover:bg-surface-hover"
        >
          Back to Resources
        </Link>
      </div>

      {loadError ? (
        <p className="rounded-xl border border-destructive/40 bg-destructive/10 p-md text-body-md text-destructive">
          {loadError}
        </p>
      ) : (
        <CreateResourceForm resourceTypes={resourceTypes} />
      )}
    </main>
  );
}
