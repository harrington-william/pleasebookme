"use client";

import { CircleCheck, CircleAlert, Info, TriangleAlert } from "lucide-react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

import {
  isGoogleConnectOutcome,
  type GoogleConnectOutcome,
} from "@/features/integrations/google/types/google-connection";
import { cn } from "@/lib/utils";


type Severity = "success" | "neutral" | "warning" | "destructive";

const OUTCOMES: Record<
  GoogleConnectOutcome,
  { severity: Severity; message: string }
> = {
  connected: {
    severity: "success",
    message: "Google account connected.",
  },
  denied: {
    severity: "neutral",
    message: "You declined the Google consent screen — nothing was changed.",
  },
  invalid_state: {
    severity: "warning",
    message:
      "This connection attempt expired or was already used. Please try again.",
  },
  missing_code: {
    severity: "warning",
    message: "Google did not return the expected response. Please try again.",
  },
  error: {
    severity: "destructive",
    message: "Something went wrong connecting your Google account.",
  },
};

const SEVERITY_STYLES: Record<Severity, string> = {
  success: "border-success/30 bg-success/10 text-success",
  neutral: "border-border bg-surface-container text-muted-foreground",
  warning: "border-warning/30 bg-warning/10 text-warning",
  destructive: "border-destructive/30 bg-destructive/10 text-destructive",
};

const SEVERITY_ICONS: Record<Severity, typeof CircleCheck> = {
  success: CircleCheck,
  neutral: Info,
  warning: TriangleAlert,
  destructive: CircleAlert,
};

export function GoogleConnectionOutcomeBanner() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const raw = searchParams.get("google");

  const [outcome] = useState<GoogleConnectOutcome | null>(() =>
    isGoogleConnectOutcome(raw) ? raw : null
  );

  useEffect(() => {
    if (!isGoogleConnectOutcome(raw)) return;

    const next = new URLSearchParams(searchParams.toString());
    next.delete("google");
    const query = next.toString();
    router.replace(query ? `${pathname}?${query}` : pathname, {
      scroll: false,
    });

    if (raw === "connected") {
      router.refresh();
    }
  }, [raw, pathname, router, searchParams]);

  if (!outcome) return null;

  const { severity, message } = OUTCOMES[outcome];
  const Icon = SEVERITY_ICONS[severity];

  return (
    <div
      role="status"
      className={cn(
        "flex items-start gap-sm rounded-lg border px-md py-sm text-body-md",
        SEVERITY_STYLES[severity]
      )}
    >
      <Icon className="mt-[2px] size-4 shrink-0" aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}
