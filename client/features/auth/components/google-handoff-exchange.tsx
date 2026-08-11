"use client";

import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";

import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { exchangeGoogleHandoff } from "@/features/auth/services/auth-api";
import { AuthRequestError } from "@/features/auth/services/auth-errors";

/**
 * Final step of one-shot registration: trade the callback's handoff code for a
 * session, then land on the dashboard.
 *
 * By the time this renders the account already exists and the Google connection
 * is already stored — the platform did all of that inside the callback. The only
 * thing still missing is the session cookie, because Spring Boot cannot write
 * one for this origin. So this is not "signing up"; it is collecting a session
 * that has already been earned.
 *
 * Which is why a failure here is genuinely awkward: the work succeeded and the
 * user still is not signed in. Their account is real, so the recovery is to sign
 * in with Google normally — not to register again.
 */

type GoogleHandoffExchangeProps = {
  /** The single-use `?handoff=` value. 60-second TTL, consumed by GETDEL. */
  code: string;
};

export function GoogleHandoffExchange({ code }: GoogleHandoffExchangeProps) {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);

  // MUST be a ref, not state. React StrictMode runs effects twice in
  // development; state resets across the double-invoke but a ref survives it.
  // Without this latch the second call hits an already-GETDEL'd key and reports
  // failure on a flow that actually succeeded — the user is told their sign-up
  // broke while their session cookie is sitting right there.
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;

    // Drop the code from the URL before anything can await. It is single-use
    // and expires in 60 seconds, so this is hygiene rather than a control — but
    // it keeps a credential-shaped string out of browser history and out of any
    // screen share. Doing it here, not in the Server Component, because only the
    // browser has a history entry to rewrite.
    window.history.replaceState(null, "", "/google/complete");

    async function exchange() {
      try {
        await exchangeGoogleHandoff(code);

        // `replace`, not `push`: the code is spent, so leaving this page in the
        // history stack means Back lands on a dead exchange screen.
        router.replace("/dashboard");
        // Server Components must re-read the cookie that was just written.
        router.refresh();
      } catch (caught) {
        setError(
          caught instanceof AuthRequestError
            ? caught.message
            : "Could not finish signing you in. Please try again."
        );
      }
    }

    void exchange();
  }, [code, router]);

  if (error) {
    return (
      <div className="space-y-md">
        <AuthFormAlert message={error} />

        <p className="text-body-md text-muted-foreground">
          Your account was created and your Google account was connected — only
          the final sign-in step failed. Sign in with Google to continue; there
          is no need to register again.
        </p>

        <Link
          href="/login"
          className="inline-flex text-label-md text-primary transition-all hover:underline"
        >
          Go to sign in
        </Link>
      </div>
    );
  }

  return (
    <div
      role="status"
      aria-live="polite"
      className="flex items-center justify-center gap-sm py-lg text-body-md text-muted-foreground"
    >
      <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
      Finishing sign-up…
    </div>
  );
}
