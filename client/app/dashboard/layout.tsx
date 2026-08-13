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

/**
 * Shell for every /dashboard route.
 *
 * SCAFFOLD ONLY. The chrome — rail, command bar, section list — is built from
 * `design/client/stitch/dashboard_pleasebookme`, but almost none of the
 * sections it advertises have a route or a platform capability behind them yet.
 * Those entries render visibly reserved rather than linking to a 404 or faking
 * data; the switch that turns one on lives in `dashboard-navigation.ts`, and
 * every reserved control names its own blocker in a comment. Do not "finish"
 * them client-side (AGENTS.md → "Reserved features").
 *
 * Authorization is *not* implemented here, deliberately. The platform has no
 * authorization engine yet, so there is nothing to gate sections against — the
 * rail shows the same thing to every signed-in actor. When RBAC lands, filter
 * `DASHBOARD_NAV_ITEMS` against the principal's permissions here, in the
 * layout, and re-check inside each page: a hidden nav item is a UX affordance,
 * never a control.
 *
 * The session check below is the same defence-in-depth pattern the pages use.
 * `proxy.ts` already gates `/dashboard`, but that check is optimistic by design
 * (cookie presence only), so the real read happens server-side here as well —
 * and the platform re-verifies the JWT on every API call regardless.
 *
 * It redirects to SESSION_EXPIRED_REDIRECT rather than a bare "/login": this
 * layout cannot clear a cookie during render, and sending a visitor to /login
 * while the refresh cookie survives makes proxy.ts send them right back. See
 * lib/auth-cookies.ts.
 */
export default async function DashboardLayout({
  children,
}: Readonly<{ children: ReactNode }>) {
  const actor = await getSessionActor();

  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  return <DashboardShell>{children}</DashboardShell>;
}
