"use client";

import { LoaderCircle } from "lucide-react";
import { useState } from "react";

import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { startGoogleConnect } from "@/features/integrations/google/services/google-connect-api";
import {
  GOOGLE_SCOPES,
  GOOGLE_SCOPE_LABELS,
  type GoogleScope,
} from "@/features/integrations/google/types/google-connection";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

/**
 * Entry point for connecting a Google account.
 *
 * Two-step, deliberately: pick which capabilities to grant, then navigate to
 * Google. Scope selection is exposed now rather than hardcoded to CALENDAR,
 * because Google's consent screen lets users untick individual scopes anyway —
 * so the granted set can already differ from the requested set, and pretending
 * otherwise in the UI would be misleading.
 *
 * A failure here happens BEFORE Google is involved (network error, expired
 * session), so it renders as an ordinary inline error, not as a `?google=`
 * outcome — those only exist on the way back from a real consent screen.
 */
export function ConnectGoogleButton() {
  const [selected, setSelected] = useState<GoogleScope[]>([...GOOGLE_SCOPES]);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function toggle(scope: GoogleScope, checked: boolean) {
    setSelected((current) =>
      checked
        ? [...current, scope]
        : current.filter((candidate) => candidate !== scope)
    );
  }

  async function onConnect() {
    setError(null);
    setPending(true);

    try {
      const { authorizationUrl } = await startGoogleConnect(selected);

      // A real top-level navigation, NOT a fetch. The browser must actually
      // land on Google's consent screen; an XHR cannot follow it.
      window.location.assign(authorizationUrl);

      // Intentionally leave `pending` true: the page is navigating away, and
      // re-enabling the button would invite a double submit in the gap.
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not start the Google connection. Please try again."
      );
      setPending(false);
    }
  }

  const nothingSelected = selected.length === 0;

  return (
    <div className="space-y-md">
      {error ? <AuthFormAlert message={error} /> : null}

      <fieldset className="space-y-sm" disabled={pending}>
        <legend className="text-label-md tracking-wider text-muted-foreground uppercase">
          Access to grant
        </legend>

        {GOOGLE_SCOPES.map((scope) => {
          const checked = selected.includes(scope);

          return (
            <div key={scope} className="flex items-start gap-sm">
              <Checkbox
                id={`scope-${scope}`}
                checked={checked}
                onCheckedChange={(next) => toggle(scope, next)}
                className="mt-[2px]"
              />
              <Label
                htmlFor={`scope-${scope}`}
                className="text-body-md text-muted-foreground"
              >
                {GOOGLE_SCOPE_LABELS[scope]}
              </Label>
            </div>
          );
        })}
      </fieldset>

      <p className="text-label-md text-muted-foreground">
        You can untick individual permissions on Google&apos;s consent screen
        too. Only what you actually approve is stored.
      </p>

      <button
        type="button"
        onClick={onConnect}
        disabled={pending || nothingSelected}
        className={cn(
          "flex w-full items-center justify-center gap-sm rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors",
          "hover:bg-primary/90",
          "focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-surface focus-visible:outline-none",
          "disabled:cursor-not-allowed disabled:opacity-70"
        )}
      >
        {pending ? (
          <>
            <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
            Redirecting to Google…
          </>
        ) : (
          "Connect Google account"
        )}
      </button>

      {nothingSelected ? (
        <p className="text-label-md text-destructive">
          Select at least one permission to continue.
        </p>
      ) : null}
    </div>
  );
}
