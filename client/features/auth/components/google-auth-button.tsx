"use client";

import { LoaderCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";

import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { GoogleMark } from "@/features/auth/components/google-mark";
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
