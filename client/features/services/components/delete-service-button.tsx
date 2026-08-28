"use client";

import { LoaderCircle, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";

import { deleteService } from "@/features/services/services/service-api";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

export function DeleteServiceButton({
  serviceId,
  title,
}: {
  serviceId: number;
  title: string;
}) {
  const router = useRouter();
  const [confirming, setConfirming] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onDelete() {
    if (!confirming) {
      setConfirming(true);
      return;
    }

    setError(null);
    setPending(true);

    try {
      await deleteService(serviceId);
      router.refresh();
      setConfirming(false);
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not delete this service. Please try again."
      );
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="flex flex-col items-end gap-xs">
      <div className="flex items-center justify-end gap-xs">
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
          onClick={onDelete}
          disabled={pending}
          aria-label={
            confirming ? `Confirm deleting ${title}` : `Delete ${title}`
          }
          title={confirming ? `Confirm deleting ${title}` : `Delete ${title}`}
          className={cn(
            "inline-flex items-center gap-xs rounded-[6px] border px-sm py-1 text-label-md transition-colors disabled:opacity-50",
            confirming
              ? "border-destructive/40 bg-destructive/10 text-destructive hover:bg-destructive/20"
              : "border-border bg-transparent text-muted-foreground hover:border-surface-hover hover:bg-surface-hover hover:text-foreground"
          )}
        >
          {pending ? (
            <LoaderCircle className="size-3.5 animate-spin" aria-hidden="true" />
          ) : (
            <Trash2 className="size-3.5" aria-hidden="true" />
          )}
        </button>
      </div>

      {error ? <p className="text-label-md text-destructive">{error}</p> : null}
    </div>
  );
}
