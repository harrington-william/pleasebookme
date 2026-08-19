import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { SignOutButton } from "@/features/auth/components/sign-out-button";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Dashboard",
};

export default async function DashboardPage() {
  const actor = await getSessionActor();

  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  return (
    <main className="mx-auto flex w-full max-w-[720px] flex-grow flex-col justify-center p-md md:p-2xl">
      <div className="relative z-10 rounded-xl border border-border bg-surface p-lg md:p-xl">
        <p className="text-label-md tracking-wider text-muted-foreground uppercase">
          Session established
        </p>

        <h1 className="mt-xs text-headline-md text-foreground">
          You are signed in
        </h1>

        <dl className="mt-lg space-y-sm border-t border-border pt-lg">
          <div className="flex items-baseline justify-between gap-md">
            <dt className="text-body-md text-muted-foreground">Actor type</dt>
            <dd className="font-mono text-mono-label text-foreground">
              {actor.actorType}
            </dd>
          </div>
          <div className="flex items-baseline justify-between gap-md">
            <dt className="text-body-md text-muted-foreground">Subject</dt>
            <dd className="font-mono text-mono-label break-all text-foreground">
              {actor.subject}
            </dd>
          </div>
          <div className="flex items-baseline justify-between gap-md">
            <dt className="text-body-md text-muted-foreground">Tenant</dt>
            <dd className="font-mono text-mono-label text-foreground">
              {actor.tenantUid ?? "— no active plan"}
            </dd>
          </div>
        </dl>

        <div className="mt-xl">
          <SignOutButton />
        </div>
      </div>
    </main>
  );
}
