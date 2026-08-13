"use client";

import Link from "next/link";

import type { DashboardNavItem } from "@/components/dashboard/dashboard-navigation";
import { cn } from "@/lib/utils";

type DashboardNavLinkProps = {
  item: DashboardNavItem;
  active: boolean;
  /** Closes the mobile drawer once a destination is chosen. */
  onNavigate?: () => void;
};

/**
 * A single rail entry.
 *
 * A reserved item is rendered as a non-interactive `<span>`, not a disabled
 * `<a>`: an anchor without an href is already inert, but it still reads as a
 * link to assistive technology. `aria-disabled` plus the muted treatment and
 * the "Soon" chip say the same thing to everyone.
 */
export function DashboardNavLink({
  item,
  active,
  onNavigate,
}: DashboardNavLinkProps) {
  const Icon = item.icon;

  const base =
    "group relative flex w-full items-center gap-sm rounded-lg px-sm py-xs text-body-md transition-colors";

  if (item.status === "reserved") {
    return (
      <span
        aria-disabled="true"
        title={item.reservedReason}
        className={cn(base, "cursor-default text-muted-foreground/60")}
      >
        <Icon className="size-4 shrink-0" aria-hidden="true" />
        <span className="truncate">{item.label}</span>
        <span className="ml-auto rounded-sm border border-border px-base py-px text-mono-label font-mono text-muted-foreground/70">
          Soon
        </span>
      </span>
    );
  }

  return (
    <Link
      href={item.href}
      onClick={onNavigate}
      aria-current={active ? "page" : undefined}
      className={cn(
        base,
        // Interactive state brightens the surface; it never lifts (DESIGN.md).
        active
          ? "bg-sidebar-accent font-medium text-sidebar-primary"
          : "text-muted-foreground hover:bg-sidebar-accent hover:text-foreground"
      )}
    >
      <Icon className="size-4 shrink-0" aria-hidden="true" />
      <span className="truncate">{item.label}</span>
      {active ? (
        <span
          aria-hidden="true"
          className="absolute inset-y-xs right-0 w-[3px] rounded-full bg-sidebar-primary"
        />
      ) : null}
    </Link>
  );
}
