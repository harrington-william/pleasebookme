import { Boxes } from "lucide-react";
import type { ReactNode } from "react";

import { clientEnv } from "@/lib/env";
import { cn } from "@/lib/utils";

type AuthCardProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
};

export function AuthCard({
  title,
  subtitle,
  children,
  footer,
  className,
}: AuthCardProps) {
  return (
    <div
      className={cn(
        "relative z-10 w-full max-w-[480px] rounded-xl border border-border bg-surface p-lg shadow-[0_20px_40px_rgba(0,0,0,0.4)] md:p-xl",
        className
      )}
    >
      <header className="mb-xl text-center">
        <div className="mb-md inline-flex size-12 items-center justify-center rounded-lg border border-border bg-surface-container">
          <Boxes className="size-6 text-primary" aria-hidden="true" />
        </div>

        <h1 className="mb-xs text-headline-lg-mobile text-foreground md:text-headline-lg">
          {title}
        </h1>

        <p className="text-body-md text-muted-foreground">{subtitle}</p>
      </header>

      {children}

      {footer ? <div className="mt-xl text-center">{footer}</div> : null}
    </div>
  );
}

export function AuthDivider({ label = "OR" }: { label?: string }) {
  return (
    <div className="relative mt-lg flex items-center justify-center">
      <div aria-hidden="true" className="absolute inset-0 flex items-center">
        <div className="w-full border-t border-border" />
      </div>
      <span className="relative bg-surface px-md font-mono text-mono-label text-muted-foreground">
        {label}
      </span>
    </div>
  );
}

export const APP_NAME = clientEnv.appName;
