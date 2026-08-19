import type { Metadata } from "next";
import { redirect } from "next/navigation";
import type { ReactNode } from "react";

import { DashboardShell } from "@/components/dashboard/dashboard-shell";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: {
    default: "Dashboard",
    template: "%s · Dashboard",
  },
};

export default async function DashboardLayout({
  children,
}: Readonly<{ children: ReactNode }>) {
  const actor = await getSessionActor();

  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  return <DashboardShell>{children}</DashboardShell>;
}
