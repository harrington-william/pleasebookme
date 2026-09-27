"use client";

import { ArrowLeft, ArrowRight, CalendarDays } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { useAvailableSlots } from "@/features/public-booking/hooks/use-available-slots";
import { BOOKING_STEPS, stepIndex, type BookingStep } from "@/features/public-booking/lib/booking-steps";
import { formatLongDate, formatSlotTime, localDateInZone } from "@/features/public-booking/lib/date-math";
import type { BookingDetails } from "@/features/public-booking/schemas/booking-details-schema";
import { createBooking, getService } from "@/features/public-booking/services/public-booking-api";
import { normalizePublicBookingError } from "@/features/public-booking/services/public-booking-errors";
import type {
  PublicBooking,
  PublicOrganization,
  PublicService,
  PublicServiceSummary,
  TimeSlot,
} from "@/features/public-booking/types/public-booking";
import { ApiRequestError } from "@/lib/api-error";

import { BookingCalendar } from "./booking-calendar";
import { BookingDetailsForm } from "./booking-details-form";
import { BookingReview } from "./booking-review";
import { BookingShell } from "./booking-shell";
import { BookingSuccess } from "./booking-success";
import { ErrorMessage } from "./error-message";
import { SelectedServiceStrip } from "./selected-service-strip";
import { ServicePicker } from "./service-picker";
import { SlotList } from "./slot-list";
import { StepHeader } from "./step-header";

export function BookingWidget({
  organization,
  initialService = null,
}: {
  organization: PublicOrganization;
  initialService?: PublicService | null;
}) {
  const router = useRouter();
  const [step, setStep] = useState<BookingStep>(initialService ? "date" : "service");
  const [service, setService] = useState<PublicService | null>(initialService);
  const [loadingServiceSlug, setLoadingServiceSlug] = useState<string | null>(null);
  const [serviceError, setServiceError] = useState<string | null>(null);
  const [visitorZone, setVisitorZone] = useState(
    () => Intl.DateTimeFormat().resolvedOptions().timeZone
  );
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [details, setDetails] = useState<BookingDetails | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [retryAfter, setRetryAfter] = useState(0);
  const [result, setResult] = useState<PublicBooking | null>(null);

  const availability = useAvailableSlots({
    organizationSlug: organization.slug,
    serviceSlug: service?.slug ?? "",
    localDate: service ? selectedDate : null,
    visitorZone,
    scheduleZone: service?.scheduleTimezone ?? organization.timezone,
  });

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [step]);

  useEffect(() => {
    if (retryAfter <= 0) return;
    const timer = window.setInterval(
      () => setRetryAfter((current) => Math.max(0, current - 1)),
      1000
    );
    return () => window.clearInterval(timer);
  }, [retryAfter]);

  // Moving forward never touches earlier choices.
  function advance(next: BookingStep) {
    setSubmitError(null);
    setStep(next);
  }

  // Stepping back keeps every choice up to the target step and clears the
  // ones made after it, so a visitor changing the date does not lose the
  // service, and changing the service does not start the flow over.
  function goToStep(target: BookingStep) {
    const index = stepIndex(target);
    if (index < stepIndex("date")) setSelectedDate(null);
    if (index < stepIndex("time")) setSelectedSlot(null);
    if (index < stepIndex("details")) setDetails(null);
    setSubmitError(null);
    setStep(target);
  }

  function goBack() {
    const previous = BOOKING_STEPS[stepIndex(step) - 1];
    if (previous) goToStep(previous.key);
  }

  function navigateCompleted(target: BookingStep) {
    if (step !== "done" && stepIndex(target) < stepIndex(step)) goToStep(target);
  }

  async function selectService(summary: PublicServiceSummary) {
    if (service?.slug === summary.slug) {
      advance("date");
      return;
    }
    setServiceError(null);
    setLoadingServiceSlug(summary.slug);
    try {
      const detail = await getService(organization.slug, summary.slug);
      setService(detail);
      setSelectedDate(null);
      setSelectedSlot(null);
      setDetails(null);
      advance("date");
    } catch {
      setServiceError("Unable to load this service. Please try again.");
    } finally {
      setLoadingServiceSlug(null);
    }
  }

  function changeZone(zone: string) {
    setVisitorZone(zone);
    if (
      selectedDate &&
      selectedSlot &&
      localDateInZone(new Date(selectedSlot.slotStart).getTime(), zone) !== selectedDate
    ) {
      setSelectedSlot(null);
    }
  }

  async function confirmBooking() {
    if (!service || !details || !selectedSlot) return;
    setSubmitting(true);
    setSubmitError(null);
    try {
      const booking = await createBooking(organization.slug, service.slug, {
        name: details.name,
        phone: details.phone,
        email: details.email || undefined,
        notes: details.notes || undefined,
        timezone: visitorZone,
        slotStart: selectedSlot.slotStart,
      });
      setResult(booking);
      setStep("done");
    } catch (error) {
      const detail = error instanceof ApiRequestError
        ? error.detail
        : { status: 0, message: "" };
      setSubmitError(normalizePublicBookingError(detail));
      if (detail.status === 409) {
        setSelectedSlot(null);
        availability.refresh();
        setStep("time");
      } else if (detail.status === 429) {
        setRetryAfter(detail.retryAfterSeconds ?? 60);
      } else if (detail.status === 400) {
        setStep("details");
      } else if (detail.status === 404) {
        router.refresh();
      }
    } finally {
      setSubmitting(false);
    }
  }

  function bookAnother() {
    setResult(null);
    goToStep("service");
  }

  return (
    <BookingShell
      organization={organization}
      service={service}
      activeStep={step}
      onNavigate={step === "done" ? undefined : navigateCompleted}
    >
      {step === "service" ? (
        <>
          <StepHeader step="service" title="Choose a Service" subtitle="Pick what you'd like to book." />
          {serviceError ? <div className="mb-md"><ErrorMessage message={serviceError} /></div> : null}
          <ServicePicker
            services={organization.services}
            selectedSlug={service?.slug ?? null}
            loadingSlug={loadingServiceSlug}
            onSelect={selectService}
          />
        </>
      ) : null}

      {step === "date" && service ? (
        <>
          <StepHeader step="date" title="Select a Date" subtitle="Choose when you'd like to visit." />
          <SelectedServiceStrip service={service} className="md:hidden" />
          <BookingCalendar
            value={selectedDate}
            onChange={(date) => {
              setSelectedDate(date);
              setSelectedSlot(null);
            }}
            visitorZone={visitorZone}
            onZoneChange={changeZone}
            weekStart={organization.weekStart}
            availableWeekdays={service.availableWeekdays}
            maximumAdvanceBooking={service.maximumAdvanceBooking}
          />
          <div className="mt-xl flex gap-md">
            <button type="button" onClick={goBack} className="h-12 flex-1 cursor-pointer rounded-lg border border-border text-body-md font-medium transition-colors hover:bg-surface-hover">Back</button>
            <button type="button" disabled={!selectedDate} onClick={() => advance("time")} className="h-12 flex-[2] cursor-pointer rounded-lg bg-primary text-body-md font-medium text-primary-foreground shadow-md transition-colors hover:bg-primary/80 disabled:cursor-not-allowed disabled:opacity-50">Continue to Time</button>
          </div>
        </>
      ) : null}

      {step === "time" && service && selectedDate ? (
        <>
          <StepHeader step="time" title="Select Time" subtitle={formatLongDate(selectedDate, visitorZone, "short")} />
          <SelectedServiceStrip service={service} className="md:hidden" />
          <div className="rounded-xl border border-border bg-surface p-lg shadow-sm">
            {submitError ? <div className="mb-md"><ErrorMessage message={submitError} /></div> : null}
            <SlotList {...availability} visitorZone={visitorZone} selected={selectedSlot} onSelect={setSelectedSlot} />
            <div className="mt-md flex items-center justify-between border-t border-border pt-lg">
              <button type="button" onClick={goBack} className="flex h-12 cursor-pointer items-center gap-xs text-body-md text-muted-foreground transition-colors hover:text-foreground"><ArrowLeft className="size-4.5" /> Back</button>
              <button type="button" disabled={!selectedSlot} onClick={() => advance("details")} className="flex h-12 min-w-[120px] cursor-pointer items-center justify-center gap-xs rounded-lg bg-primary px-lg text-body-md font-medium text-primary-foreground transition-colors hover:bg-primary/80 disabled:cursor-not-allowed disabled:opacity-50">Continue <ArrowRight className="size-4.5" /></button>
            </div>
          </div>
        </>
      ) : null}

      {step === "details" && service && selectedDate && selectedSlot ? (
        <>
          <StepHeader step="details" title="Your Details" subtitle="Please provide your contact information to finalize the booking." />
          <p className="mb-lg flex items-center gap-xs rounded-lg bg-primary/10 px-sm py-xs text-body-md text-primary"><CalendarDays className="size-4" /> {formatLongDate(selectedDate, visitorZone, "short")} · {formatSlotTime(selectedSlot.slotStart, visitorZone)} ({visitorZone})</p>
          {submitError ? <div className="mb-md"><ErrorMessage message={submitError} /></div> : null}
          <BookingDetailsForm initialValues={details} onBack={goBack} onSubmit={(values) => { setDetails(values); advance("review"); }} />
        </>
      ) : null}

      {step === "review" && service && selectedSlot && details ? (
        <>
          <StepHeader step="review" title="Review Booking" subtitle="Please review your appointment details before confirming." />
          <BookingReview
            service={service}
            slot={selectedSlot}
            details={details}
            visitorZone={visitorZone}
            submitting={submitting}
            submitError={submitError}
            retryAfter={retryAfter}
            onEditService={() => goToStep("service")}
            onEditDate={() => goToStep("date")}
            onEditDetails={() => goToStep("details")}
            onConfirm={confirmBooking}
          />
        </>
      ) : null}

      {step === "done" && service && result ? (
        <BookingSuccess service={service} booking={result} visitorZone={visitorZone} onBookAnother={bookAnother} />
      ) : null}
    </BookingShell>
  );
}
