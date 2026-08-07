"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import type {
  GoogleButtonConfiguration,
  GoogleCredentialResponse,
} from "@/features/auth/types/google-identity";
import { clientEnv } from "@/lib/env";

/**
 * Loads Google Identity Services and renders its real sign-in button.
 *
 * WHY GOOGLE'S OWN BUTTON, RATHER THAN POSTING FROM OUR OWN:
 * an ID token can only be obtained through GIS's own UI — Google does not
 * expose an API that mints one from an arbitrary click. So the real button has
 * to exist and has to be what the user actually clicks. The visual treatment is
 * handled by the caller (see google-auth-button.tsx), which stacks our styled
 * button underneath and makes Google's transparent.
 *
 * The script is injected once per document and reused: GIS registers globals,
 * so loading it twice is wasteful and can re-trigger One Tap.
 */

const GIS_SRC = "https://accounts.google.com/gsi/client";
const GIS_SCRIPT_ID = "google-identity-services";

type GoogleIdentityStatus = "idle" | "loading" | "ready" | "unavailable";

/** The async half of the status — everything else is derived during render. */
type LoadState = "loading" | "ready" | "unavailable";

type UseGoogleIdentityOptions = {
  /** Fired with the ID token once the user completes Google's flow. */
  onCredential: (idToken: string) => void;
  /** Skip loading entirely (e.g. the feature flag is off). */
  disabled?: boolean;
  /**
   * Rendered width in pixels. Google requires an explicit number (it caps at
   * 400) and will not stretch to its container, so callers that overlay the
   * button on their own styling must measure and pass the real width, or the
   * clickable area will not match what the user sees.
   */
  width?: number;
  buttonOptions?: GoogleButtonConfiguration;
};

function loadGisScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (typeof document === "undefined") {
      reject(new Error("Google Identity Services requires a browser."));
      return;
    }

    if (window.google?.accounts?.id) {
      resolve();
      return;
    }

    const existing = document.getElementById(
      GIS_SCRIPT_ID
    ) as HTMLScriptElement | null;

    if (existing) {
      existing.addEventListener("load", () => resolve(), { once: true });
      existing.addEventListener(
        "error",
        () => reject(new Error("Failed to load Google Identity Services.")),
        { once: true }
      );
      return;
    }

    const script = document.createElement("script");
    script.id = GIS_SCRIPT_ID;
    script.src = GIS_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () =>
      reject(new Error("Failed to load Google Identity Services."));

    document.head.appendChild(script);
  });
}

export function useGoogleIdentity({
  onCredential,
  disabled = false,
  width,
  buttonOptions,
}: UseGoogleIdentityOptions) {
  // Only the ASYNC outcome of loading the script is state. Whether the feature
  // is disabled or misconfigured is knowable during render, so it is derived
  // below — writing it into state from an effect would trigger a second render
  // pass for something already known on the first.
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const containerRef = useRef<HTMLDivElement | null>(null);

  const misconfigured = !clientEnv.googleClientId;

  const status: GoogleIdentityStatus = disabled
    ? "idle"
    : misconfigured
      ? "unavailable"
      : loadState;

  // Keeps the GIS callback stable while always calling the latest handler —
  // GIS captures the callback once at initialize() and never re-reads it.
  const onCredentialRef = useRef(onCredential);
  useEffect(() => {
    onCredentialRef.current = onCredential;
  }, [onCredential]);

  const handleCredential = useCallback(
    (response: GoogleCredentialResponse) => {
      if (response?.credential) {
        onCredentialRef.current(response.credential);
      }
    },
    []
  );

  useEffect(() => {
    // Without a client ID GIS renders nothing and logs an opaque console
    // error, so skip loading entirely — `status` already reports "unavailable".
    if (disabled || misconfigured) return;

    let cancelled = false;

    loadGisScript()
      .then(() => {
        if (cancelled) return;

        const id = window.google?.accounts?.id;
        const container = containerRef.current;

        if (!id || !container) {
          setLoadState("unavailable");
          return;
        }

        id.initialize({
          client_id: clientEnv.googleClientId,
          callback: handleCredential,
          // Never sign a returning user in without an explicit click: an
          // automatic session change on page load is surprising and, on a
          // shared machine, wrong.
          auto_select: false,
          use_fedcm_for_prompt: true,
        });

        // renderButton appends; clear first so a width change re-renders
        // rather than stacking a second button underneath the old one.
        container.replaceChildren();
        id.renderButton(container, {
          type: "standard",
          theme: "filled_black",
          size: "large",
          shape: "rectangular",
          logo_alignment: "left",
          ...(width ? { width: Math.min(Math.round(width), 400) } : {}),
          ...buttonOptions,
        });

        setLoadState("ready");
      })
      .catch(() => {
        if (!cancelled) setLoadState("unavailable");
      });

    return () => {
      cancelled = true;
    };
    // buttonOptions is intentionally not a dependency: callers pass an inline
    // object literal, which would re-render the Google button on every render.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [disabled, misconfigured, handleCredential, width]);

  return { containerRef, status };
}
