import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { CreateResourceTypeForm } from "@/features/resources/components/create-resource-type-form";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Create Resource Type",
  description: "Add a reusable category for reservable assets.",
};

export default async function CreateResourceTypePage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-lg p-md md:p-2xl">
      <div className="space-y-xs">
        <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
          Create Resource Type
        </h1>
        <p className="text-body-md text-muted-foreground">
          Define a reusable category before adding resources of that type.
        </p>
      </div>

      <CreateResourceTypeForm />
    </main>
  );
}
