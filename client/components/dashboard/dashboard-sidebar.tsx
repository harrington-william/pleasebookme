"use client";

import { LogOut } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";

import { DashboardNavLink } from "@/components/dashboard/dashboard-nav-link";
import {
  DASHBOARD_ACCOUNT_ITEMS,
  DASHBOARD_NAV_ITEMS,
  isDashboardNavItemActive,
} from "@/components/dashboard/dashboard-navigation";
import { QuickCreateButton } from "@/components/dashboard/quick-create-button";
import { logout } from "@/features/auth/services/auth-api";
import { clientEnv } from "@/lib/env";
import { cn } from "@/lib/utils";

type DashboardSidebarProps = {
  className?: string;
  onNavigate?: () => void;
};

export function DashboardSidebar({
  className,
  onNavigate,
}: DashboardSidebarProps) {
  const pathname = usePathname();

  return (
    <div
      className={cn(
        "flex h-full w-[280px] shrink-0 flex-col border-r border-sidebar-border bg-sidebar",
        className
      )}
    >
      <Link
        href="/dashboard"
        onClick={onNavigate}
        className="flex items-center gap-sm border-b border-sidebar-border px-md py-md"
      >
        <span
          aria-hidden="true"
          className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary text-body-md font-semibold text-primary-foreground"
        >
          P
        </span>
        <span className="flex min-w-0 flex-col">
          <span className="truncate text-body-lg font-semibold text-foreground">
            {clientEnv.appName}
          </span>
          <span className="truncate text-label-md text-muted-foreground">
            Enterprise Infrastructure
          </span>
        </span>
      </Link>

      <nav
        aria-label="Dashboard sections"
        className="flex-1 overflow-y-auto px-sm py-md"
      >
        <ul className="flex flex-col gap-base">
          {DASHBOARD_NAV_ITEMS.map((item) => (
            <li key={item.href}>
              <DashboardNavLink
                item={item}
                active={isDashboardNavItemActive(item, pathname)}
                onNavigate={onNavigate}
              />
            </li>
          ))}
        </ul>
      </nav>

      <div className="border-t border-sidebar-border p-sm">
        <QuickCreateButton className="w-full" />

        <ul className="mt-sm flex flex-col gap-base">
          {DASHBOARD_ACCOUNT_ITEMS.map((item) => (
            <li key={item.href}>
              <DashboardNavLink
                item={item}
                active={isDashboardNavItemActive(item, pathname)}
                onNavigate={onNavigate}
              />
            </li>
          ))}
          <li>
            <SignOutNavItem />
          </li>
        </ul>
      </div>
    </div>
  );
}

function SignOutNavItem() {
  const router = useRouter();
  const [pending, setPending] = useState(false);

  async function onSignOut() {
    setPending(true);
    try {
      await logout();
      router.push("/login");
      router.refresh();
    } finally {
      setPending(false);
    }
  }

  return (
    <button
      type="button"
      onClick={onSignOut}
      disabled={pending}
      className="flex w-full items-center gap-sm rounded-lg px-sm py-xs text-body-md text-muted-foreground transition-colors hover:bg-sidebar-accent hover:text-foreground disabled:opacity-50"
    >
      <LogOut className="size-4 shrink-0" aria-hidden="true" />
      <span className="truncate">{pending ? "Signing out…" : "Logout"}</span>
    </button>
  );
}
