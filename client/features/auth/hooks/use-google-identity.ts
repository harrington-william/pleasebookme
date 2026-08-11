"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import type {
  GoogleButtonConfiguration,
  GoogleCredentialResponse,
} from "@/features/auth/types/google-identity";
import { clientEnv } from "@/lib/env";

const GIS_SRC = "https://accounts.google.com/gsi/client";
const GIS_SCRIPT_ID = "google-identity-services";

type GoogleIdentityStatus = "idle" | "loading" | "ready" | "unavailable";

type LoadState = "loading" | "ready" | "unavailable";

type UseGoogleIdentityOptions = {
  onCredential: (idToken: string) => void;
  disabled?: boolean;
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
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const containerRef = useRef<HTMLDivElement | null>(null);

  const misconfigured = !clientEnv.googleClientId;

  const status: GoogleIdentityStatus = disabled
    ? "idle"
    : misconfigured
      ? "unavailable"
      : loadState;

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
          auto_select: false,
          use_fedcm_for_prompt: true,
        });

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

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [disabled, misconfigured, handleCredential, width]);

  return { containerRef, status };
}
