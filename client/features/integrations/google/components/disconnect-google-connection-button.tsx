"use client";

import { LoaderCircle, Unplug } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";

import { disconnectGoogleConnection } from "@/features/integrations/google/services/google-connect-api";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

export function DisconnectGoogleConnectionButton({
  oauthConnectionUid,
  providerEmail,
}: {
  oauthConnectionUid: string;
  providerEmail: string;
}) {
  const router = useRouter();
  const [confirming, setConfirming] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onDisconnect() {
    if (!confirming) {
      setConfirming(true);
      return;
    }

    setError(null);
    setPending(true);

    try {
      await disconnectGoogleConnection(oauthConnectionUid);
      router.refresh();
      setConfirming(false);
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not disconnect. Please try again."
      );
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="flex flex-col items-end gap-xs">
      <div className="flex items-center gap-xs">
        {confirming && !pending ? (
          <button
            type="button"
            onClick={() => setConfirming(false)}
            className="rounded-lg px-sm py-[6px] text-label-md text-muted-foreground transition-colors hover:text-foreground"
          >
            Cancel
          </button>
        ) : null}

        <button
          type="button"
          onClick={onDisconnect}
          disabled={pending}
          aria-label={
            confirming
              ? `Confirm disconnecting ${providerEmail}`
              : `Disconnect ${providerEmail}`
          }
          className={cn(
            "inline-flex items-center gap-xs rounded-lg border px-sm py-[6px] text-label-md transition-colors disabled:opacity-50",
            confirming
              ? "border-destructive/40 bg-destructive/10 text-destructive hover:bg-destructive/20"
              : "border-border bg-surface-container text-foreground hover:border-surface-hover hover:bg-surface-hover"
          )}
        >
          {pending ? (
            <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
          ) : (
            <Unplug className="size-4" aria-hidden="true" />
          )}
          {pending
            ? "Disconnecting…"
            : confirming
              ? "Confirm disconnect"
              : "Disconnect"}
        </button>
      </div>

      {confirming && !pending ? (
        <p className="text-label-md text-muted-foreground">
          This revokes access at Google immediately.
        </p>
      ) : null}

      {error ? <p className="text-label-md text-destructive">{error}</p> : null}
    </div>
  );
}
