"use client";

import { Check, Copy, KeyRound, LoaderCircle } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useFormContext, useWatch } from "react-hook-form";

import type { WidgetFormValues } from "@/features/widgets/schemas/widget-schema";
import { generateWidgetCredentials } from "@/features/widgets/services/widget-api";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

const COPIED_FEEDBACK_MS = 1500;
const MASKED_SECRET = "••••••••••••••••••••••••";

const ghostButtonClassName =
  "inline-flex h-9 items-center justify-center gap-xs rounded-lg border border-border px-md text-label-md text-foreground transition-colors hover:bg-surface-hover disabled:cursor-not-allowed disabled:opacity-50";

type WidgetCredentialPanelProps =
  | { mode: "create" }
  | { mode: "edit"; storedPublicKey: string };

/**
 * The only component in the client that ever holds a plaintext secret. The
 * pair lives in form state between "Generate" and Save, and nowhere else — no
 * storage, no context, no URL — so navigating away simply discards it.
 */
export function WidgetCredentialPanel(props: WidgetCredentialPanelProps) {
  const { control, setValue } = useFormContext<WidgetFormValues>();
  const credentials = useWatch({ control, name: "credentials" });
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function generate() {
    setError(null);
    setPending(true);

    try {
      const pair = await generateWidgetCredentials();
      setValue("credentials", pair, { shouldValidate: true, shouldDirty: true });
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not generate a key pair. Please try again."
      );
    } finally {
      setPending(false);
    }
  }

  function discardRotation() {
    setError(null);
    setValue("credentials", null, { shouldValidate: true, shouldDirty: true });
  }

  if (credentials) {
    return (
      <div className="rounded-lg border border-border bg-background">
        <KeyRow label="Public key" value={credentials.publicKey} />
        <KeyRow label="Secret key" value={credentials.secretKey}>
          <p
            role="note"
            className="mt-xs flex items-start gap-xs text-label-md text-warning"
          >
            <span
              aria-hidden="true"
              className="mt-[5px] size-1.5 shrink-0 rounded-full bg-warning"
            />
            Shown once. Copy it now — after you save, only a hash is stored and
            it cannot be recovered.
          </p>
        </KeyRow>

        <div className="flex flex-col gap-xs px-md py-sm">
          <div className="flex flex-wrap items-center gap-xs">
            {props.mode === "create" ? (
              <GenerateButton
                label="Regenerate"
                pending={pending}
                onClick={generate}
              />
            ) : (
              <button
                type="button"
                onClick={discardRotation}
                disabled={pending}
                className={ghostButtonClassName}
              >
                Discard rotation
              </button>
            )}
          </div>

          {props.mode === "edit" ? (
            <p className="text-label-md text-muted-foreground">
              This pair replaces the current one when you save.
            </p>
          ) : null}

          {error ? (
            <p className="text-label-md text-destructive">{error}</p>
          ) : null}
        </div>
      </div>
    );
  }

  if (props.mode === "edit") {
    return (
      <div className="rounded-lg border border-border bg-background">
        <KeyRow label="Public key" value={props.storedPublicKey} />

        <div className="border-b border-border px-md py-sm">
          <p className="text-label-md tracking-wider text-muted-foreground uppercase">
            Secret key
          </p>
          <p className="mt-base font-mono text-mono-label text-muted-foreground">
            {MASKED_SECRET}
          </p>
          <p className="mt-base text-label-md text-muted-foreground">
            Hashed at rest — not retrievable.
          </p>
        </div>

        <div className="flex flex-col gap-xs px-md py-sm">
          <div>
            <GenerateButton
              label="Rotate key pair"
              pending={pending}
              onClick={generate}
            />
          </div>
          <p className="text-label-md text-muted-foreground">
            Rotating replaces both keys when you save; embeds using the current
            pair will stop authenticating.
          </p>
          {error ? (
            <p className="text-label-md text-destructive">{error}</p>
          ) : null}
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center gap-sm rounded-lg border border-dashed border-border bg-background p-lg text-center">
      <KeyRound className="size-5 text-muted-foreground" aria-hidden="true" />
      <div className="space-y-base">
        <p className="text-body-md text-foreground">No key pair yet</p>
        <p className="text-label-md text-muted-foreground">
          Generate a public/secret pair to authenticate this widget.
        </p>
      </div>
      <GenerateButton
        label="Generate key pair"
        pending={pending}
        onClick={generate}
      />
      {error ? <p className="text-label-md text-destructive">{error}</p> : null}
    </div>
  );
}

function GenerateButton({
  label,
  pending,
  onClick,
}: {
  label: string;
  pending: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={pending}
      className={ghostButtonClassName}
    >
      {pending ? (
        <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
      ) : (
        <KeyRound className="size-4" aria-hidden="true" />
      )}
      {label}
    </button>
  );
}

function KeyRow({
  label,
  value,
  children,
}: {
  label: string;
  value: string;
  children?: React.ReactNode;
}) {
  return (
    <div className="border-b border-border px-md py-sm">
      <div className="flex items-center justify-between gap-md">
        <p className="text-label-md tracking-wider text-muted-foreground uppercase">
          {label}
        </p>
        <CopyValueButton label={`Copy ${label.toLowerCase()}`} value={value} />
      </div>
      <p className="mt-base font-mono text-mono-label break-all text-foreground select-all">
        {value}
      </p>
      {children}
    </div>
  );
}

function CopyValueButton({ label, value }: { label: string; value: string }) {
  const [copied, setCopied] = useState(false);
  const [failed, setFailed] = useState(false);
  const resetTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (resetTimer.current) clearTimeout(resetTimer.current);
    };
  }, []);

  async function copy() {
    setFailed(false);

    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);

      if (resetTimer.current) clearTimeout(resetTimer.current);
      resetTimer.current = setTimeout(
        () => setCopied(false),
        COPIED_FEEDBACK_MS
      );
    } catch {
      // Clipboard access is denied in some contexts (insecure origin, permission
      // policy). The value stays selectable, so the fallback is to say so.
      setFailed(true);
    }
  }

  return (
    <span className="flex items-center gap-xs">
      {failed ? (
        <span className="text-label-md text-destructive">
          Could not copy — select the text instead.
        </span>
      ) : null}

      <button
        type="button"
        onClick={copy}
        aria-label={label}
        title={label}
        className={cn(
          "inline-flex size-7 items-center justify-center rounded-[6px] border border-border transition-colors hover:bg-surface-hover",
          copied ? "text-success" : "text-muted-foreground hover:text-foreground"
        )}
      >
        {copied ? (
          <Check className="size-3.5" aria-hidden="true" />
        ) : (
          <Copy className="size-3.5" aria-hidden="true" />
        )}
      </button>

      <span aria-live="polite" className="sr-only">
        {copied ? "Copied" : ""}
      </span>
    </span>
  );
}
