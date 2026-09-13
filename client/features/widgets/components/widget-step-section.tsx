import type { ReactNode } from "react";

export function WidgetStepSection({
  id,
  eyebrow,
  title,
  action,
  children,
}: {
  id: string;
  eyebrow: string;
  title: string;
  action?: ReactNode;
  children: ReactNode;
}) {
  return (
    <section
      id={id}
      className="scroll-mt-lg space-y-md rounded-xl border border-border bg-surface p-lg"
    >
      <div className="flex items-start justify-between gap-md">
        <div className="space-y-base">
          <p className="font-mono text-mono-label text-muted-foreground uppercase">
            {eyebrow}
          </p>
          <h2 className="text-headline-md text-foreground">{title}</h2>
        </div>
        {action}
      </div>

      {children}
    </section>
  );
}
