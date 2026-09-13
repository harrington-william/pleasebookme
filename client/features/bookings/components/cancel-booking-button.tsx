"use client";

import { LoaderCircle, XCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";

import { cancelBooking } from "@/features/bookings/services/booking-api";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

export function CancelBookingButton({
  bookingId,
  title,
  disabled = false,
}: {
  bookingId: number;
  title: string;
  disabled?: boolean;
}) {
  const router = useRouter();
  const [confirming, setConfirming] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onCancel() {
    if (!confirming) {
      setConfirming(true);
      return;
    }

    setError(null);
    setPending(true);

    try {
      await cancelBooking(bookingId);
      router.refresh();
      setConfirming(false);
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not cancel this booking. Please try again."
      );
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="flex flex-col items-stretch gap-xs">
      <div className="flex items-center gap-xs">
        {confirming && !pending ? (
          <button
            type="button"
            onClick={() => setConfirming(false)}
            className="rounded-lg px-sm py-[6px] text-label-md text-muted-foreground transition-colors hover:text-foreground"
          >
            Keep
          </button>
        ) : null}

        <button
          type="button"
          onClick={onCancel}
          disabled={disabled || pending}
          aria-label={confirming ? `Confirm cancelling ${title}` : `Cancel ${title}`}
          title={confirming ? `Confirm cancelling ${title}` : `Cancel ${title}`}
          className={cn(
            "inline-flex h-9 flex-1 items-center justify-center gap-xs rounded-lg border px-md text-label-md transition-colors disabled:cursor-not-allowed disabled:opacity-50",
            confirming
              ? "border-destructive/40 bg-destructive/10 text-destructive hover:bg-destructive/20"
              : "border-border bg-surface-container text-foreground hover:bg-surface-hover"
          )}
        >
          {pending ? (
            <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
          ) : (
            <XCircle className="size-4" aria-hidden="true" />
          )}
          {pending ? "Cancelling..." : confirming ? "Confirm cancel" : "Cancel"}
        </button>
      </div>

      {confirming && !pending ? (
        <p className="text-label-md text-muted-foreground">
          This marks the booking as cancelled immediately.
        </p>
      ) : null}

      {error ? <p className="text-label-md text-destructive">{error}</p> : null}
    </div>
  );
}
