"use client";

import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

import {
  SERVICE_EDITOR_TABS,
  SERVICE_TAB_QUERY_KEY,
  type ServiceEditorTab,
  type ServiceTabId,
} from "@/features/services/components/service-editor-navigation";
import { cn } from "@/lib/utils";

function groupTabs(): Map<string, ServiceEditorTab[]> {
  return SERVICE_EDITOR_TABS.reduce<Map<string, ServiceEditorTab[]>>(
    (accumulator, tab) => {
      const existing = accumulator.get(tab.group) ?? [];
      accumulator.set(tab.group, [...existing, tab]);
      return accumulator;
    },
    new Map()
  );
}

type ServiceEditorShellProps = {
  title: string;
  description?: string;
  activeTab: ServiceTabId;
  children: ReactNode;
};

export function ServiceEditorShell({
  title,
  description,
  activeTab,
  children,
}: ServiceEditorShellProps) {
  const pathname = usePathname();
  const groups = groupTabs();

  return (
    <main className="flex w-full flex-1 flex-col lg:flex-row">
      <aside className="shrink-0 border-b border-border lg:sticky lg:top-0 lg:h-screen lg:w-[240px] lg:border-r lg:border-b-0">
        <div className="flex flex-col gap-md p-md lg:p-lg">
          <Link
            href="/dashboard/services"
            className="inline-flex items-center gap-xs text-body-md text-muted-foreground transition-colors hover:text-foreground"
          >
            <ArrowLeft className="size-4" aria-hidden="true" />
            Back
          </Link>

          <nav
            aria-label="Service editor sections"
            className="flex flex-row gap-md lg:flex-col"
          >
            {[...groups.entries()].map(([group, tabs]) => (
              <div key={group} className="flex flex-1 flex-col gap-base">
                <p className="text-label-md tracking-wider text-muted-foreground uppercase">
                  {group}
                </p>

                <ul className="flex flex-col gap-base">
                  {tabs.map((tab) => {
                    const active = tab.id === activeTab;
                    const Icon = tab.icon;

                    return (
                      <li key={tab.id}>
                        <Link
                          // scroll={false}: a tab switch is a change of view, not
                          // a new document — jumping to the top on every click
                          // loses the user's place in a long panel.
                          scroll={false}
                          href={`${pathname}?${SERVICE_TAB_QUERY_KEY}=${tab.id}`}
                          aria-current={active ? "page" : undefined}
                          className={cn(
                            "flex items-center gap-sm rounded-lg px-sm py-xs text-body-md transition-colors",
                            active
                              ? "bg-surface-hover font-medium text-foreground"
                              : "text-muted-foreground hover:bg-surface-hover hover:text-foreground"
                          )}
                        >
                          <Icon className="size-4 shrink-0" aria-hidden="true" />
                          <span className="truncate">{tab.label}</span>
                        </Link>
                      </li>
                    );
                  })}
                </ul>
              </div>
            ))}
          </nav>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col gap-lg p-md md:p-2xl">
        <div className="space-y-xs">
          <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
            {title}
          </h1>
          {description ? (
            <p className="text-body-md text-muted-foreground">{description}</p>
          ) : null}
        </div>

        {children}
      </div>
    </main>
  );
}
