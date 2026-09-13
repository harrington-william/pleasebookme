"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState, type ReactNode } from "react";
import { useFormContext, useWatch } from "react-hook-form";

import {
  WIDGET_STEP_QUERY_KEY,
  type WidgetStepId,
} from "@/features/widgets/components/widget-editor-navigation";
import { WidgetStepSection } from "@/features/widgets/components/widget-step-section";
import {
  DEFAULT_ORIGIN_LABEL,
  type WidgetFormValues,
} from "@/features/widgets/schemas/widget-schema";
import { WIDGET_TYPE_LABELS } from "@/features/widgets/types/widget";
import { cn } from "@/lib/utils";

const MASKED_SECRET_PREFIX = "pbm_sk_••••••••••••";

type WidgetReviewStepProps =
  | { mode: "create" }
  | { mode: "edit"; storedPublicKey: string };

export function WidgetReviewStep(props: WidgetReviewStepProps) {
  const { control } = useFormContext<WidgetFormValues>();
  const values = useWatch({ control });
  const pathname = usePathname();

  const name = values.name?.trim() ?? "";
  const type = values.type ?? "INLINE";
  const origin = values.origin?.trim() ?? "";
  const credentials = values.credentials ?? null;
  const publicKey = credentials?.publicKey ?? (props.mode === "edit" ? props.storedPublicKey : null);

  function editLink(step: WidgetStepId) {
    return (
      <Link
        scroll={false}
        href={`${pathname}?${WIDGET_STEP_QUERY_KEY}=${step}`}
        className="rounded-[6px] border border-border px-sm py-1 text-label-md text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground"
      >
        Edit
      </Link>
    );
  }

  return (
    <>
      <WidgetStepSection
        id="review-basics"
        eyebrow="Step 03 · Review"
        title="Basics"
        action={editLink("basics")}
      >
        <dl>
          <ReviewRow label="Name">
            {name ? (
              name
            ) : (
              <span className="text-destructive">Required</span>
            )}
          </ReviewRow>
          <ReviewRow label="Type">{WIDGET_TYPE_LABELS[type]}</ReviewRow>
          {props.mode === "edit" ? (
            <ReviewRow label="Status">
              {values.enabled ? "Enabled" : "Disabled"}
            </ReviewRow>
          ) : null}
        </dl>
      </WidgetStepSection>

      <WidgetStepSection
        id="review-security"
        eyebrow="Step 03 · Review"
        title="Security"
        action={editLink("security")}
      >
        <dl>
          <ReviewRow label="Allowed origin">
            {origin ? (
              <span className="font-mono text-mono-label break-all">{origin}</span>
            ) : (
              <span className="flex flex-col items-end gap-base">
                <span className="text-muted-foreground">Any site</span>
                <span className="text-label-md text-muted-foreground">
                  Shown as {DEFAULT_ORIGIN_LABEL} on the widget card
                </span>
              </span>
            )}
          </ReviewRow>

          <ReviewRow label="Public key">
            {publicKey ? (
              <span className="font-mono text-mono-label break-all">{publicKey}</span>
            ) : (
              <span className="text-destructive">No key pair generated</span>
            )}
          </ReviewRow>

          <ReviewRow label="Secret key">
            <SecretKeyValue mode={props.mode} secretKey={credentials?.secretKey ?? null} />
          </ReviewRow>
        </dl>
      </WidgetStepSection>
    </>
  );
}

function ReviewRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-md border-b border-border py-sm last:border-b-0">
      <dt className="shrink-0 text-label-md tracking-wider text-muted-foreground uppercase">
        {label}
      </dt>
      <dd className="min-w-0 text-right text-body-md text-foreground">
        {children}
      </dd>
    </div>
  );
}

/**
 * Masked by default even though the user may still need to copy it — the
 * Security step is one click away, and a review screen is exactly where a
 * shoulder-surfer would look.
 */
function SecretKeyValue({
  mode,
  secretKey,
}: {
  mode: "create" | "edit";
  secretKey: string | null;
}) {
  const [revealed, setRevealed] = useState(false);

  if (!secretKey) {
    return mode === "edit" ? (
      <span className="text-muted-foreground">Unchanged</span>
    ) : (
      <span className="text-destructive">No key pair generated</span>
    );
  }

  return (
    <span className="flex flex-col items-end gap-base">
      <span className="flex items-center gap-xs">
        <span
          className={cn(
            "font-mono text-mono-label break-all",
            revealed ? "text-foreground" : "text-muted-foreground"
          )}
        >
          {revealed ? secretKey : MASKED_SECRET_PREFIX}
        </span>
        <button
          type="button"
          onClick={() => setRevealed((value) => !value)}
          aria-pressed={revealed}
          className="rounded-[6px] border border-border px-sm py-1 text-label-md text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground"
        >
          {revealed ? "Hide" : "Reveal"}
        </button>
      </span>

      {mode === "edit" ? (
        <span className="text-label-md text-warning">
          Will replace the current pair when you save.
        </span>
      ) : null}
    </span>
  );
}
