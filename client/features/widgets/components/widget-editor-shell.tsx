"use client";

import { ArrowLeft, Check } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

import {
  WIDGET_EDITOR_STEPS,
  WIDGET_STEP_QUERY_KEY,
  type WidgetStepId,
} from "@/features/widgets/components/widget-editor-navigation";
import { cn } from "@/lib/utils";

type WidgetEditorShellProps = {
  title: string;
  description: string;
  activeStep: WidgetStepId;
  completedSteps: ReadonlySet<WidgetStepId>;
  children: ReactNode;
};

/**
 * A wizard reads top to bottom, so the stepper sits above the form rather than
 * in the services editor's left rail. Every cell is a plain link: gating lives
 * in Continue and Save, not here, so someone going back to fix a value is never
 * trapped on a later step.
 */
export function WidgetEditorShell({
  title,
  description,
  activeStep,
  completedSteps,
  children,
}: WidgetEditorShellProps) {
  const pathname = usePathname();

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-lg p-md md:p-2xl">
      <Link
        href="/dashboard/widgets"
        className="inline-flex w-fit items-center gap-xs text-body-md text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft className="size-4" aria-hidden="true" />
        Back to widgets
      </Link>

      <div className="space-y-xs">
        <h1 className="text-headline-lg-mobile text-foreground md:text-headline-lg">
          {title}
        </h1>
        <p className="text-body-md text-muted-foreground">{description}</p>
      </div>

      <nav aria-label="Widget setup steps">
        <ol className="flex flex-col gap-xs rounded-xl border border-border bg-surface p-xs md:flex-row">
          {WIDGET_EDITOR_STEPS.map((step) => {
            const active = step.id === activeStep;
            const complete = !active && completedSteps.has(step.id);

            return (
              <li key={step.id} className="flex-1">
                <Link
                  // scroll={false}: a step switch is a change of view, not a
                  // new document — jumping to the top would lose the reader's
                  // place in a long panel.
                  scroll={false}
                  href={`${pathname}?${WIDGET_STEP_QUERY_KEY}=${step.id}`}
                  aria-current={active ? "step" : undefined}
                  className={cn(
                    "flex items-center gap-sm rounded-lg px-md py-sm transition-colors",
                    active
                      ? "bg-surface-hover"
                      : "hover:bg-surface-hover/60"
                  )}
                >
                  <span
                    aria-hidden="true"
                    className={cn(
                      "flex size-6 shrink-0 items-center justify-center rounded-[6px] border font-mono text-mono-label",
                      active && "border-primary/30 bg-primary/10 text-primary",
                      complete && "border-success/30 bg-success/10 text-success",
                      !active && !complete && "border-border text-muted-foreground"
                    )}
                  >
                    {complete ? <Check className="size-3.5" /> : step.numeral}
                  </span>

                  <span className="flex min-w-0 flex-col">
                    <span
                      className={cn(
                        "text-body-md",
                        active ? "text-foreground" : "text-muted-foreground"
                      )}
                    >
                      {step.label}
                    </span>
                    <span className="truncate text-label-md text-muted-foreground">
                      {step.description}
                    </span>
                  </span>
                </Link>
              </li>
            );
          })}
        </ol>
      </nav>

      {children}
    </main>
  );
}
