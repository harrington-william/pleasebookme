"use client";

import { LogOut } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";

import { logout } from "@/features/auth/services/auth-api";

/**
 * Ends the local session and returns to sign-in.
 *
 * See app/api/auth/logout/route.ts: this clears the browser's cookies but the
 * platform exposes no revocation endpoint yet, so the refresh token stays
 * valid server-side until it expires.
 */
export function SignOutButton() {
  const router = useRouter();
  const [pending, setPending] = useState(false);

  async function onSignOut() {
    setPending(true);
    try {
      await logout();
      router.push("/login");
      router.refresh();
    } finally {
      setPending(false);
    }
  }

  return (
    <button
      type="button"
      onClick={onSignOut}
      disabled={pending}
      className="inline-flex items-center gap-xs rounded-lg border border-border bg-surface-container px-md py-sm text-label-md text-foreground transition-colors hover:border-surface-hover hover:bg-surface-hover disabled:opacity-50"
    >
      <LogOut className="size-4" aria-hidden="true" />
      {pending ? "Signing out…" : "Sign out"}
    </button>
  );
}
