import { Calendar, CalendarCheck, CalendarPlus, Check, CirclePlus, Clock, MapPin } from "lucide-react";
import { googleCalendarUrl } from "../../../../../core/lib/calendar-link";
import { formatLongDate, formatSlotTime, localDateInZone } from "../../../../../core/lib/date-math";
import { formatDuration } from "../../../../../core/lib/format";
import type { Booking, Service } from "../../../logic/types";

export function BookingSuccess({
  service,
  booking,
  visitorZone,
  onBookAnother,
}: {
  service: Service;
  booking: Booking;
  visitorZone: string;
  onBookAnother: () => void;
}) {
  const accepted = booking.status === "ACCEPTED";
  const date = localDateInZone(new Date(booking.startTime).getTime(), visitorZone);
  const calendarUrl = googleCalendarUrl({
    title: `${service.title} with ${service.organization.name}`,
    startTime: booking.startTime,
    endTime: booking.endTime,
    location: service.location,
  });
  return (
    <div className="flex flex-col items-center text-center">
      <div className="mb-lg flex size-24 items-center justify-center rounded-full bg-primary text-primary-foreground">
        <Check className="size-12" aria-hidden="true" />
      </div>
      <h1 className="text-headline-lg-mobile md:text-headline-lg">{accepted ? "Booking Confirmed!" : "Request Sent"}</h1>
      <p className="mt-xs mb-xl text-body-lg text-muted-foreground">
        {accepted ? "Your appointment has been successfully scheduled." : `${service.organization.name} will confirm your appointment shortly.`}
      </p>
      <div className="mb-xl flex w-full flex-col gap-md rounded-xl border border-border bg-surface-container p-lg text-left shadow-sm">
        <div className="flex items-center justify-between border-b border-border pb-md text-body-md">
          <span className="text-muted-foreground">Booking Reference</span>
          <span className="font-bold text-primary">#PBM-{booking.bookingUid.slice(0, 8).toUpperCase()}</span>
        </div>
        <div className="flex items-center gap-md">
          <span className="flex size-12 items-center justify-center rounded-lg bg-surface-hover"><CalendarCheck className="size-5 text-muted-foreground" /></span>
          <div><h2 className="text-headline-sm">{service.title}</h2><p className="text-body-md text-muted-foreground">with {service.organization.name}</p></div>
        </div>
        <div className="flex flex-col gap-xs text-body-md">
          <p className="flex items-center gap-xs"><Calendar className="size-4.5 text-muted-foreground" />{formatLongDate(date, visitorZone, "long")}</p>
          <p className="flex items-center gap-xs"><Clock className="size-4.5 text-muted-foreground" />{formatSlotTime(booking.startTime, visitorZone)} – {formatSlotTime(booking.endTime, visitorZone)} ({formatDuration(service.durationMinutes)})</p>
          {service.location ? <p className="flex items-center gap-xs"><MapPin className="size-4.5 text-muted-foreground" />{service.location}</p> : null}
        </div>
      </div>
      <div className="flex w-full flex-col gap-md">
        {service.successRedirectUrl ? (
          <a href={service.successRedirectUrl} className="flex h-12 cursor-pointer items-center justify-center rounded-lg bg-primary text-body-md font-medium text-primary-foreground transition-colors hover:bg-primary/80">Return to Website</a>
        ) : null}
        <div className="grid grid-cols-1 gap-md md:grid-cols-2">
          <a href={calendarUrl} target="_blank" rel="noreferrer" className="flex h-12 cursor-pointer items-center justify-center gap-xs rounded-lg border border-border text-body-md font-medium transition-colors hover:bg-surface-hover"><CalendarPlus className="size-4.5" /> Add to Google Calendar</a>
          <button type="button" onClick={onBookAnother} className="flex h-12 cursor-pointer items-center justify-center gap-xs rounded-lg border border-border text-body-md font-medium transition-colors hover:bg-surface-hover"><CirclePlus className="size-4.5" /> Book Another</button>
        </div>
      </div>
    </div>
  );
}
