"use client";

import { useEffect, useMemo, useState } from "react";

import { localDateInZone, scheduleDatesFor } from "../../../core/lib/date-math";

import { ApiRequestError, type ApiError } from "../../../core/api/errors";
import type { BookingApi, TimeSlot } from "./types";

interface SlotState {
  key: string | null;
  slots: TimeSlot[];
  error: string | null;
}

// Props are destructured rather than read off an `input` object so the effect's
// dependency list names the values it actually uses. Reading `input.api.getSlots`
// inside the effect would force the whole object into the list, and every caller
// passing a fresh object literal would then refetch on every render.
export function useAvailableSlots({
  api,
  onError,
  serviceSlug,
  localDate,
  visitorZone,
  scheduleZone,
}: {
  api: BookingApi;
  onError: (error: ApiError) => void;
  serviceSlug: string;
  localDate: string | null;
  visitorZone: string;
  scheduleZone: string;
}) {
  const [refreshCount, setRefreshCount] = useState(0);
  const scheduleDates = useMemo(
    () =>
      localDate
        ? scheduleDatesFor(localDate, visitorZone, scheduleZone)
        : [],
    [localDate, scheduleZone, visitorZone]
  );
  const requestKey = localDate
    ? `${serviceSlug}|${scheduleDates.join(",")}|${localDate}|${visitorZone}|${refreshCount}`
    : null;
  const [state, setState] = useState<SlotState>({
    key: null,
    slots: [],
    error: null,
  });

  useEffect(() => {
    if (!requestKey || !localDate) {
      return;
    }

    let active = true;

    // A visitor day can overlap two schedule-zone dates, so fetch both and
    // keep only instants that still belong to the selected visitor day.
    Promise.all(scheduleDates.map((date) => api.getSlots(serviceSlug, date)))
      .then((responses) => {
        if (!active) return;
        const seen = new Set<string>();
        const slots = responses
          .flatMap((response) => response.slots)
          .filter(
            (slot) =>
              localDateInZone(
                new Date(slot.slotStart).getTime(),
                visitorZone
              ) === localDate
          )
          .sort((first, second) =>
            first.slotStart.localeCompare(second.slotStart)
          )
          .filter((slot) => {
            if (seen.has(slot.slotStart)) return false;
            seen.add(slot.slotStart);
            return true;
          });
        setState({ key: requestKey, slots, error: null });
      })
      .catch((error: unknown) => {
        if (active) {
          const detail = error instanceof ApiRequestError
            ? error.detail
            : { status: 0, message: "Unable to load available times." };
          onError(detail);
          setState({
            key: requestKey,
            slots: [],
            error: "Unable to load available times.",
          });
        }
      });

    return () => {
      active = false;
    };
  }, [api, onError, serviceSlug, localDate, visitorZone, requestKey, scheduleDates]);

  return {
    slots: state.key === requestKey ? state.slots : [],
    // Loading derives from the request identity so stale responses cannot clear it.
    loading: requestKey !== null && state.key !== requestKey,
    error: state.key === requestKey ? state.error : null,
    refresh: () => setRefreshCount((current) => current + 1),
  };
}
