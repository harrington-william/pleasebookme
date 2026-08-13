"use client";

import { Bell, CircleUser, Menu, Search, Settings } from "lucide-react";

import { QuickCreateButton } from "@/components/dashboard/quick-create-button";

type DashboardTopbarProps = {
  onOpenNavigation: () => void;
};

export function DashboardTopbar({ onOpenNavigation }: DashboardTopbarProps) {
  return (
    <header className="sticky top-0 z-20 flex h-16 shrink-0 items-center gap-sm border-b border-border bg-background px-md">
      <button
        type="button"
        onClick={onOpenNavigation}
        aria-label="Open navigation"
        className="flex size-9 shrink-0 items-center justify-center rounded-lg border border-border text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground lg:hidden"
      >
        <Menu className="size-4" aria-hidden="true" />
      </button>

      {/* RESERVED — there is no search endpoint on the platform, and the ⌘K
          palette the mock implies does not exist. Disabled rather than wired to
          a client-side filter that would silently search nothing. */}
      <div className="relative min-w-0 flex-1 md:max-w-[480px]">
        <Search
          className="pointer-events-none absolute top-1/2 left-sm size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />
        <input
          type="search"
          disabled
          placeholder="Search bookings, customers, or resources…"
          aria-label="Search (not available yet)"
          title="Platform search has not been built yet."
          className="h-9 w-full rounded-lg border border-border bg-surface pr-14 pl-2xl text-body-md text-foreground placeholder:text-muted-foreground disabled:cursor-not-allowed"
        />
        <kbd
          aria-hidden="true"
          className="pointer-events-none absolute top-1/2 right-sm -translate-y-1/2 rounded-sm border border-border px-base py-px font-mono text-mono-label text-muted-foreground"
        >
          ⌘K
        </kbd>
      </div>

      <div className="ml-auto flex shrink-0 items-center gap-xs">
        {/* RESERVED — no notification feed endpoint. The mock's unread count is
            deliberately not rendered: an invented number is worse than none. */}
        <TopbarIconButton
          label="Notifications"
          reason="The notification feed has not been built yet."
          icon={Bell}
        />

        {/* RESERVED — /dashboard/settings does not exist yet. The one settings
            page that does (integrations) is reachable from the rail. */}
        <TopbarIconButton
          label="Settings"
          reason="The settings index has not been built yet."
          icon={Settings}
        />

        <QuickCreateButton className="hidden sm:inline-flex" />

        {/* RESERVED — the platform exposes no /me endpoint, so there is no
            name, email or avatar to show and no account menu to open
            (AGENTS.md → "Platform contract & known gaps"). */}
        <TopbarIconButton
          label="Account"
          reason="The platform exposes no /me endpoint yet."
          icon={CircleUser}
        />
      </div>
    </header>
  );
}

function TopbarIconButton({
  label,
  reason,
  icon: Icon,
}: {
  label: string;
  reason: string;
  icon: typeof Bell;
}) {
  return (
    <button
      type="button"
      disabled
      aria-label={label}
      title={reason}
      className="flex size-9 items-center justify-center rounded-lg text-muted-foreground transition-colors disabled:opacity-50"
    >
      <Icon className="size-4" aria-hidden="true" />
    </button>
  );
}
