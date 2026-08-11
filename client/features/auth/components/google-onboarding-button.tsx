"use client";

import { LoaderCircle } from "lucide-react";
import { useState } from "react";

import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { GoogleMark } from "@/features/auth/components/google-mark";
import { startGoogleOnboarding } from "@/features/auth/services/auth-api";
import { ApiRequestError } from "@/lib/api-error";
import { clientEnv } from "@/lib/env";
import { cn } from "@/lib/utils";

/**
 * One-shot registration entry point.
 *
 * Creates the account AND grants Calendar/Sheets/Drive in a single Google round
 * trip, so a new admin lands on a workspace that can actually compute a booking
 * rather than an empty one with an integration chore waiting in settings.
 *
 * NOT the same control as GoogleAuthButton, despite looking almost identical:
 *
 *   GoogleAuthButton (login)   Google Identity Services renders its own button,
 *                              which we overlay, because an ID token can only be
 *                              minted by Google's own UI. No consent screen for
 *                              a returning user.
 *
 *   this (register)            An ordinary button. It asks our platform for an
 *                              authorization URL and navigates there. Google's
 *                              JavaScript is not involved at all.
 *
 * That difference is why this file has no ResizeObserver, no width measuring,
 * and no transparent overlay — there is nothing of Google's to line up with.
 *
 * "Continue", not "Sign up": the platform resolves an existing Google identity
 * to its existing account, so clicking this with a known account simply signs
 * that user in. The copy should not claim to create something it will not.
 */
export function GoogleOnboardingButton() {
  const featureEnabled = clientEnv.googleOAuthEnabled;

  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onContinue() {
    setError(null);
    setPending(true);

    try {
      const { authorizationUrl } = await startGoogleOnboarding();

      // A real top-level navigation, NOT a fetch. `fetch` follows redirects
      // transparently, so fetching this would try to load Google's consent
      // screen as a cross-origin XHR — a failure that presents as CORS and is
      // not one.
      window.location.assign(authorizationUrl);

      // `pending` stays true on purpose: the page is navigating away, and
      // re-enabling the button would invite a double submit in the gap.
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not start Google sign-up. Please try again."
      );
      setPending(false);
    }
  }

  return (
    <div className="mt-lg">
      {error ? (
        <div className="mb-md">
          <AuthFormAlert message={error} />
        </div>
      ) : null}

      <button
        type="button"
        onClick={onContinue}
        disabled={!featureEnabled || pending}
        className={cn(
          "flex w-full items-center justify-center gap-sm rounded-lg border border-border bg-surface-container px-md py-sm transition-colors",
          "text-label-md text-foreground",
          "focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-surface focus-visible:outline-none",
          featureEnabled && !pending
            ? "hover:border-surface-hover hover:bg-surface-hover"
            : "cursor-not-allowed opacity-50"
        )}
      >
        {pending ? (
          <>
            <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
            Redirecting to Google…
          </>
        ) : (
          <>
            <GoogleMark />
            Continue with Google
          </>
        )}
      </button>

      {featureEnabled ? (
        <p className="mt-xs text-center text-label-md text-muted-foreground">
          Creates your account and connects your calendar in one step. You can
          untick individual permissions on Google&apos;s screen.
        </p>
      ) : (
        <p className="mt-xs text-center text-label-md text-muted-foreground">
          Google sign-up is coming soon.
        </p>
      )}
    </div>
  );
}
