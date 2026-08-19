"use client";

import { X } from "lucide-react";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";

import { DashboardSidebar } from "@/components/dashboard/dashboard-sidebar";
import { DashboardTopbar } from "@/components/dashboard/dashboard-topbar";

export function DashboardShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const [navigationOpen, setNavigationOpen] = useState(false);
  const [renderedPathname, setRenderedPathname] = useState(pathname);

  if (renderedPathname !== pathname) {
    setRenderedPathname(pathname);
    setNavigationOpen(false);
  }

  useEffect(() => {
    if (!navigationOpen) return;

    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setNavigationOpen(false);
      }
    }

    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [navigationOpen]);

  return (
    <div className="flex w-full flex-1 items-stretch">
      <DashboardSidebar className="sticky top-0 hidden h-screen lg:flex" />

      <div className="flex min-w-0 flex-1 flex-col">
        <DashboardTopbar onOpenNavigation={() => setNavigationOpen(true)} />

        {/* Deliberately not a <main>: the pages under /dashboard render their
            own, and two main landmarks on one document is a lint-level
            accessibility error. */}
        <div className="flex flex-1 flex-col">{children}</div>
      </div>

      {navigationOpen ? (
        <div className="fixed inset-0 z-50 lg:hidden">
          <button
            type="button"
            aria-label="Close navigation"
            onClick={() => setNavigationOpen(false)}
            className="absolute inset-0 bg-background/80"
          />

          <div
            role="dialog"
            aria-modal="true"
            aria-label="Dashboard navigation"
            className="absolute inset-y-0 left-0 flex max-w-[85vw]"
          >
            <DashboardSidebar onNavigate={() => setNavigationOpen(false)} />

            <button
              type="button"
              aria-label="Close navigation"
              onClick={() => setNavigationOpen(false)}
              className="absolute top-md right-[-44px] flex size-9 items-center justify-center rounded-lg border border-border bg-surface text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground"
            >
              <X className="size-4" aria-hidden="true" />
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
