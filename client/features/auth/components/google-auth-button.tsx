"use client";

import { LoaderCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";

import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { useGoogleIdentity } from "@/features/auth/hooks/use-google-identity";
import { signInWithGoogle } from "@/features/auth/services/auth-api";
import { AuthRequestError } from "@/features/auth/services/auth-errors";
import { clientEnv } from "@/lib/env";
import { cn } from "@/lib/utils";

/**
 * Google Sign-In (authentication).
 *
 * Answers "who is this user" and produces a normal platform session. This is
 * NOT the Google integration/consent flow that grants Calendar or Sheets
 * access — that is features/integrations/google, a different concern with a
 * different endpoint. They share nothing on the frontend.
 *
 * HOW THE VISUAL WORKS: an ID token can only be obtained through Google's own
 * rendered button — there is no API that mints one from an arbitrary click. So
 * Google's real button is present and is what actually receives the click, but
 * it is made transparent and stacked on top of our own styled button, which
 * provides the visible treatment. The wrapper is measured so Google's fixed-
 * pixel button matches our full-width control exactly; without that the
 * clickable region would not line up with what the user sees.
 */

type GoogleAuthButtonProps = {
  /* Title: "Sign up with Google" */
  label: string;
};

export function GoogleAuthButton({ label }: GoogleAuthButtonProps) {
  const router = useRouter();
  const featureEnabled = clientEnv.googleOAuthEnabled;

  const wrapperRef = useRef<HTMLDivElement | null>(null);
  const [width, setWidth] = useState<number | undefined>(undefined);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Google's button takes a pixel width and will not stretch, so track the
  // real rendered width of our control and re-render it whenever that changes.
  useEffect(() => {
    const element = wrapperRef.current;
    if (!element || !featureEnabled) return;

    setWidth(element.getBoundingClientRect().width);

    if (typeof ResizeObserver === "undefined") return;

    const observer = new ResizeObserver((entries) => {
      const next = entries[0]?.contentRect.width;
      if (next) setWidth(next);
    });

    observer.observe(element);
    return () => observer.disconnect();
  }, [featureEnabled]);

  const onCredential = useCallback(
    async (idToken: string) => {
      setError(null);
      setIsSubmitting(true);

      try {
        await signInWithGoogle(idToken);
        router.push("/dashboard");
        router.refresh();
      } catch (caught) {
        setError(
          caught instanceof AuthRequestError
            ? caught.message
            : "Could not sign in with Google. Please try again."
        );
        setIsSubmitting(false);
      }
    },
    [router]
  );

  const { containerRef, status } = useGoogleIdentity({
    onCredential,
    disabled: !featureEnabled,
    width,
  });

  // Only accept clicks once Google's button is actually mounted underneath —
  // otherwise the styled surface looks live but nothing would happen.
  const interactive = featureEnabled && status === "ready" && !isSubmitting;

  return (
    <div className="mt-lg">
      {error ? (
        <div className="mb-md">
          <AuthFormAlert message={error} />
        </div>
      ) : null}

      <div ref={wrapperRef} className="relative">
        {/* Visible layer. Decorative only: Google's button above it owns the
            interaction and the accessible name, so this is hidden from
            assistive tech to avoid announcing the control twice. */}
        <div
          aria-hidden="true"
          className={cn(
            "flex w-full items-center justify-center gap-sm rounded-lg border border-border bg-surface-container px-md py-sm transition-colors",
            "text-label-md text-foreground",
            interactive
              ? "hover:border-surface-hover hover:bg-surface-hover"
              : "opacity-50"
          )}
        >
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" />
              <span>Signing in…</span>
            </>
          ) : (
            <>
              <GoogleMark />
              <span>{label}</span>
            </>
          )}
        </div>

        {/* Google's real button: transparent, stretched over the visible layer.
            `opacity-0` rather than `hidden` or `visibility:hidden` — it must
            stay hit-testable and focusable. */}
        <div
          ref={containerRef}
          className={cn(
            "absolute inset-0 flex items-center justify-center overflow-hidden opacity-0",
            interactive ? "cursor-pointer" : "pointer-events-none"
          )}
        />
      </div>

      {featureEnabled && status === "unavailable" ? (
        <p className="mt-xs text-center text-label-md text-muted-foreground">
          Google sign-in is unavailable right now.
        </p>
      ) : null}

      {!featureEnabled ? (
        <p className="mt-xs text-center text-label-md text-muted-foreground">
          Google sign-in is coming soon.
        </p>
      ) : null}
    </div>
  );
}

/** Google's four-colour mark. Inlined so no external asset request is made. */
function GoogleMark() {
  return (
    <svg className="size-5" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M12.0003 4.75C13.7703 4.75 15.3553 5.36002 16.6053 6.54998L20.0303 3.125C17.9502 1.19 15.2353 0 12.0003 0C7.31028 0 3.25527 2.69 1.28027 6.60998L5.27028 9.70498C6.21525 6.86002 8.87028 4.75 12.0003 4.75Z"
        fill="#EA4335"
      />
      <path
        d="M23.49 12.275C23.49 11.49 23.415 10.73 23.3 10H12V14.51H18.47C18.18 15.99 17.34 17.25 16.08 18.1L19.945 21.1C22.2 19.01 23.49 15.92 23.49 12.275Z"
        fill="#4285F4"
      />
      <path
        d="M5.26498 14.2949C5.02498 13.5699 4.88501 12.7999 4.88501 11.9999C4.88501 11.1999 5.01998 10.4299 5.26498 9.7049L1.275 6.60986C0.46 8.22986 0 10.0599 0 11.9999C0 13.9399 0.46 15.7699 1.28 17.3899L5.26498 14.2949Z"
        fill="#FBBC05"
      />
      <path
        d="M12.0004 24.0001C15.2404 24.0001 17.9654 22.935 19.9454 21.095L16.0804 18.095C15.0054 18.82 13.6204 19.245 12.0004 19.245C8.8704 19.245 6.21537 17.135 5.26538 14.29L1.27539 17.385C3.25539 21.31 7.3104 24.0001 12.0004 24.0001Z"
        fill="#34A853"
      />
    </svg>
  );
}
