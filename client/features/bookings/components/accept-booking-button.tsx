"use client";

import { CheckCircle2, LoaderCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";

import { acceptBooking } from "@/features/bookings/services/booking-api";
import { ApiRequestError } from "@/lib/api-error";

export function AcceptBookingButton({
  bookingId,
  title,
  disabled = false,
}: {
  bookingId: number;
  title: string;
  disabled?: boolean;
}) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onAccept() {
    setError(null);
    setPending(true);

    try {
      await acceptBooking(bookingId);
      router.refresh();
    } catch (caught) {
      setError(
        caught instanceof ApiRequestError
          ? caught.message
          : "Could not accept this booking. Please try again."
      );
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="flex flex-col items-stretch gap-xs">
      <button
        type="button"
        onClick={onAccept}
        disabled={disabled || pending}
        aria-label={`Accept ${title}`}
        title={`Accept ${title}`}
        className="inline-flex h-9 w-full items-center justify-center gap-xs rounded-lg border border-success/40 bg-success/10 px-md text-label-md text-success cursor-pointer transition-colors hover:bg-success/20 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {pending ? (
          <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
        ) : (
          <CheckCircle2 className="size-4" aria-hidden="true" />
        )}
        {pending ? "Accepting..." : "Accept"}
      </button>

      {error ? <p className="text-label-md text-destructive">{error}</p> : null}
    </div>
  );
}
