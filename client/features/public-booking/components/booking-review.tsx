"use client";

import { ArrowRight, Clock, MapPin, Pencil } from "lucide-react";

import { formatShortDate, formatSlotTime, formatZoneLabel, localDateInZone } from "@/features/public-booking/lib/date-math";
import { formatDuration, formatPriceRange } from "@/features/public-booking/lib/format";
import type { BookingDetails } from "@/features/public-booking/schemas/booking-details-schema";
import type { PublicService, TimeSlot } from "@/features/public-booking/types/public-booking";

function EditButton({ onClick, label }: { onClick: () => void; label: string }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={label}
      className="flex cursor-pointer items-center gap-base text-label-md text-primary transition-colors hover:text-primary/80"
    >
      <Pencil className="size-3.5" aria-hidden="true" /> Edit
    </button>
  );
}

export function BookingReview({
  service,
  slot,
  details,
  visitorZone,
  submitting,
  submitError,
  retryAfter,
  onEditService,
  onEditDate,
  onEditDetails,
  onConfirm,
}: {
  service: PublicService;
  slot: TimeSlot;
  details: BookingDetails;
  visitorZone: string;
  submitting: boolean;
  submitError: string | null;
  retryAfter: number;
  onEditService: () => void;
  onEditDate: () => void;
  onEditDetails: () => void;
  onConfirm: () => void;
}) {
  const localDate = localDateInZone(new Date(slot.slotStart).getTime(), visitorZone);
  const price = formatPriceRange(service.minPrice, service.maxPrice, service.currency);
  return (
    <div>
      <div className="mb-xl grid grid-cols-1 gap-md md:grid-cols-2">
        <div className="rounded-xl border border-border bg-surface p-md">
          <div className="flex items-start justify-between gap-sm">
            <div>
              <p className="text-label-md font-semibold tracking-wider text-muted-foreground uppercase">Service</p>
              <h2 className="mt-xs text-headline-sm">{service.title}</h2>
              <p className="mt-base text-body-lg text-primary">{price}</p>
            </div>
            <EditButton onClick={onEditService} label="Change service" />
          </div>
        </div>
        <div className="rounded-xl border border-border bg-surface p-md">
          <div className="flex items-start justify-between gap-sm">
            <div>
              <p className="text-label-md font-semibold tracking-wider text-muted-foreground uppercase">Date &amp; Time</p>
              <h2 className="mt-xs text-headline-sm">{formatShortDate(localDate)}, {formatSlotTime(slot.slotStart, visitorZone)}</h2>
              <p className="mt-base flex items-center gap-xs text-body-md text-muted-foreground"><Clock className="size-4" /> {formatDuration(service.durationMinutes)}</p>
              <p className="mt-base text-label-md text-muted-foreground">{formatZoneLabel(visitorZone)}</p>
            </div>
            <EditButton onClick={onEditDate} label="Change date and time" />
          </div>
        </div>
        {service.location ? (
          <div className="rounded-xl border border-border bg-surface p-md md:col-span-2">
            <div className="flex items-start justify-between gap-sm">
              <div>
                <p className="text-label-md font-semibold tracking-wider text-muted-foreground uppercase">Location</p>
                <p className="mt-xs flex items-center gap-xs text-body-lg"><MapPin className="size-4" /> {service.location}</p>
              </div>
              <EditButton onClick={onEditService} label="Change service" />
            </div>
          </div>
        ) : null}
        <div className="rounded-xl border border-border bg-surface-container p-md md:col-span-2">
          <div className="flex items-start justify-between gap-sm">
            <div>
              <p className="text-label-md font-semibold tracking-wider text-muted-foreground uppercase">Your Details</p>
              <p className="mt-xs text-body-lg font-medium">{details.name}</p>
              <p className="text-body-md text-muted-foreground">{details.phone}</p>
              {details.email ? <p className="text-body-md text-muted-foreground">{details.email}</p> : null}
              {details.notes ? <p className="mt-xs line-clamp-2 text-body-md text-muted-foreground">{details.notes}</p> : null}
            </div>
            <EditButton onClick={onEditDetails} label="Change your details" />
          </div>
        </div>
      </div>
      <div className="flex flex-col gap-md border-t border-border pt-lg">
        <div className="flex items-center justify-between">
          <span className="text-body-lg font-medium">Price</span>
          <span className="text-headline-sm">{price}</span>
        </div>
        {submitError ? <p className="text-body-md text-destructive">{submitError}</p> : null}
        <button
          type="button"
          disabled={submitting || retryAfter > 0}
          onClick={onConfirm}
          className="flex h-12 w-full cursor-pointer items-center justify-center gap-xs rounded-lg bg-primary px-lg text-body-md font-medium text-primary-foreground shadow-lg shadow-primary/20 transition-colors hover:bg-primary/80 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {submitting ? "Booking…" : retryAfter > 0 ? `Try again in ${retryAfter}s` : "Confirm Booking"}
          <ArrowRight className="size-5" aria-hidden="true" />
        </button>
        <p className="text-center text-label-md text-muted-foreground opacity-70">
          {service.autoConfirm ? "You'll get your confirmation right away." : `${service.organization.name} will confirm your request.`}
        </p>
      </div>
    </div>
  );
}
