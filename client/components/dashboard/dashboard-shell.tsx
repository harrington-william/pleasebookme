"use client";

import { X } from "lucide-react";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";

import { DashboardSidebar } from "@/components/dashboard/dashboard-sidebar";
import { DashboardTopbar } from "@/components/dashboard/dashboard-topbar";

/**
 * The dashboard chrome: persistent rail, command bar, and the content stage.
 *
 * A client component purely to own the mobile drawer's open state — `children`
 * arrives already rendered on the server and passes straight through, so pages
 * under /dashboard stay Server Components and keep their data access
 * server-side.
 *
 * Scrolling is the document's, not a nested container's. The rail is
 * `sticky h-screen` and the command bar `sticky top-0`, which keeps both
 * pinned without introducing a second scroll context that would break
 * scroll-into-view and anchor links inside pages.
 */
export function DashboardShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const [navigationOpen, setNavigationOpen] = useState(false);
  const [renderedPathname, setRenderedPathname] = useState(pathname);

  // Choosing a destination should dismiss the drawer. Keyed on the pathname
  // rather than the link's onClick alone so a redirect or a programmatic
  // navigation closes it too.
  //
  // Adjusted during render rather than in an effect — React's documented
  // pattern for deriving state from a changed input. An effect here would
  // render the drawer open on the new route first and then close it, which is
  // both a visible flash and a lint error (react-hooks/set-state-in-effect).
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
