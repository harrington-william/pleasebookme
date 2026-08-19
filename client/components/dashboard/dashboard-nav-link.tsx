"use client";

import Link from "next/link";

import type { DashboardNavItem } from "@/components/dashboard/dashboard-navigation";
import { cn } from "@/lib/utils";

type DashboardNavLinkProps = {
  item: DashboardNavItem;
  active: boolean;
  onNavigate?: () => void;
};

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
