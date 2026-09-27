"use client";

import { useCallback, useEffect, useState } from "react";
import { useAvailableSlots } from "./use-available-slots";
import { BOOKING_STEPS, stepIndex, type BookingStep } from "./steps";
import { localDateInZone } from "../../../core/lib/date-math";
import type { BookingDetails } from "./details-schema";
import type { BookingApi, Booking, Organization, Service, ServiceSummary, TimeSlot } from "./types";
import { ApiRequestError, normalizeBookingError, type ApiError } from "../../../core/api/errors";

export function useBookingFlow(api: BookingApi) {
  const [status, setStatus] = useState<"loading" | "ready" | "unavailable">("loading");
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [step, setStep] = useState<BookingStep>("service");
  const [service, setService] = useState<Service | null>(null);
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
  const [result, setResult] = useState<Booking | null>(null);

  const handleReadError = useCallback((detail: ApiError) => {
    if ([401, 403, 404].includes(detail.status)) setStatus("unavailable");
    if (detail.status === 429) setRetryAfter(detail.retryAfterSeconds ?? 60);
  }, []);

  useEffect(() => {
    let active = true;
    api.getOrganization().then((organization) => {
      if (active) { setOrganization(organization); setStatus("ready"); }
    }).catch((error: unknown) => {
      if (active) {
        setStatus("unavailable");
        if (error instanceof ApiRequestError) handleReadError(error.detail);
      }
    });
    return () => { active = false; };
  }, [api, handleReadError]);
  const availability = useAvailableSlots({
    api,
    onError: handleReadError,
    serviceSlug: service?.slug ?? "",
    localDate: service ? selectedDate : null,
    visitorZone,
    scheduleZone: service?.scheduleTimezone ?? organization?.timezone ?? "UTC",
  });

  useEffect(() => {
    if (retryAfter <= 0) return;
    const timer = setInterval(
      () => setRetryAfter((current) => Math.max(0, current - 1)),
      1000
    );
    return () => clearInterval(timer);
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

  async function selectService(summary: ServiceSummary) {
    if (retryAfter > 0) return;
    if (service?.slug === summary.slug) {
      advance("date");
      return;
    }
    setServiceError(null);
    setLoadingServiceSlug(summary.slug);
    try {
      const detail = await api.getService(summary.slug);
      setService(detail);
      setSelectedDate(null);
      setSelectedSlot(null);
      setDetails(null);
      advance("date");
    } catch (error) {
      if (error instanceof ApiRequestError) handleReadError(error.detail);
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
    if (!service || !details || !selectedSlot || submitting || retryAfter > 0) return;
    setSubmitting(true);
    setSubmitError(null);
    try {
      const booking = await api.createBooking(service.slug, {
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
      setSubmitError(normalizeBookingError(detail));
      if (detail.status === 409) {
        setSelectedSlot(null);
        availability.refresh();
        setStep("time");
      } else if (detail.status === 429) {
        setRetryAfter(detail.retryAfterSeconds ?? 60);
      } else if (detail.status === 400) {
        setStep("details");
      } else if (detail.status === 404 || detail.status === 401 || detail.status === 403) {
        setStatus("unavailable");
      }
    } finally {
      setSubmitting(false);
    }
  }

  function bookAnother() {
    setResult(null);
    goToStep("service");
  }

  function selectDate(date: string) {
    setSelectedDate(date);
    setSelectedSlot(null);
  }
  function submitDetails(values: BookingDetails) {
    setDetails(values);
    advance("review");
  }

  return {
    status, organization, step, service, visitorZone, selectedDate, selectedSlot,
    details, submitting, submitError, retryAfter, result, loadingServiceSlug,
    serviceError, availability,
    actions: { selectService, changeZone, selectDate, selectSlot: setSelectedSlot,
      submitDetails, confirmBooking, goToStep, goBack, navigateCompleted,
      bookAnother, advance },
  };
}
