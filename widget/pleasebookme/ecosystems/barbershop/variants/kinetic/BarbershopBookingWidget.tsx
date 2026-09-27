"use client";

import { ArrowLeft, ArrowRight, CalendarDays } from "lucide-react";
import { useEffect, useRef } from "react";
import { usePleaseBookMe } from "../../../../core/auth/use-pleasebookme";
import { useBookingFlow } from "../../logic/use-booking-flow";
import { formatLongDate, formatSlotTime } from "../../../../core/lib/date-math";
import { Spinner } from "../../../../core/ui/spinner";
import { ErrorMessage } from "../../../../core/ui/error-message";
import { BookingCalendar } from "./components/booking-calendar";
import { BookingDetailsForm } from "./components/booking-details-form";
import { BookingReview } from "./components/booking-review";
import { BookingShell } from "./components/booking-shell";
import { BookingSuccess } from "./components/booking-success";
import { SelectedServiceStrip } from "./components/selected-service-strip";
import { ServicePicker } from "./components/service-picker";
import { SlotList } from "./components/slot-list";
import { StepHeader } from "./components/step-header";

export function BarbershopBookingWidget() {
  const { api } = usePleaseBookMe();
  const flow = useBookingFlow(api);
  const root = useRef<HTMLDivElement>(null);
  const { status, organization, step, service, visitorZone, selectedDate, selectedSlot,
    details, submitting, submitError, retryAfter, result, loadingServiceSlug, serviceError, availability } = flow;
  const { selectService, changeZone, selectDate, selectSlot, submitDetails, confirmBooking,
    goToStep, goBack, navigateCompleted, bookAnother, advance } = flow.actions;

  // Scroll the widget's own content, an embedded flow must not move the host page.
  useEffect(() => { root.current?.querySelector("[data-pbm-scroll]")?.scrollTo(0, 0); }, [step]);

  if (status === "loading") return (
    <div className="pbm-widget flex flex-1 flex-col items-center justify-center bg-background p-lg text-foreground">
      <Spinner label="Loading booking page…" />
    </div>
  );
  if (status === "unavailable" || !organization) return (
    <div className="pbm-widget flex flex-1 flex-col items-center justify-center bg-background p-xl text-center text-foreground">
      <h1 className="text-headline-md">This booking page isn’t available.</h1>
      <p className="mt-sm text-body-md text-muted-foreground">{retryAfter > 0 ? `Too many attempts. Try again in ${retryAfter}s.` : "Check the link you were given, or contact the business directly."}</p>
    </div>
  );
  return (
    <div ref={root} className="pbm-widget flex flex-1 flex-col bg-background text-foreground">
    {retryAfter > 0 && step !== "review" ? <p role="status" className="px-lg pt-lg text-body-md">Too many attempts. Try again in {retryAfter}s.</p> : null}
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
            onChange={selectDate}
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
            <SlotList {...availability} visitorZone={visitorZone} selected={selectedSlot} onSelect={selectSlot} />
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
          <BookingDetailsForm initialValues={details} onBack={goBack} onSubmit={submitDetails} />
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
    </div>
  );
}
