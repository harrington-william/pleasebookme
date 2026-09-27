"use client";

import { useEffect, useMemo, useState } from "react";

import { localDateInZone, scheduleDatesFor } from "@/features/public-booking/lib/date-math";
import { getSlots } from "@/features/public-booking/services/public-booking-api";
import type { TimeSlot } from "@/features/public-booking/types/public-booking";

interface SlotState {
  key: string | null;
  slots: TimeSlot[];
  error: string | null;
}

export function useAvailableSlots(input: {
  organizationSlug: string;
  serviceSlug: string;
  localDate: string | null;
  visitorZone: string;
  scheduleZone: string;
}) {
  const [refreshCount, setRefreshCount] = useState(0);
  const scheduleDates = useMemo(
    () =>
      input.localDate
        ? scheduleDatesFor(
            input.localDate,
            input.visitorZone,
            input.scheduleZone
          )
        : [],
    [input.localDate, input.scheduleZone, input.visitorZone]
  );
  const requestKey = input.localDate
    ? `${input.organizationSlug}|${input.serviceSlug}|${scheduleDates.join(",")}|${input.localDate}|${input.visitorZone}|${refreshCount}`
    : null;
  const [state, setState] = useState<SlotState>({
    key: null,
    slots: [],
    error: null,
  });

  useEffect(() => {
    if (!requestKey || !input.localDate) {
      return;
    }

    let active = true;

    // A visitor day can overlap two schedule-zone dates, so fetch both and
    // keep only instants that still belong to the selected visitor day.
    Promise.all(
      scheduleDates.map((date) =>
        getSlots(input.organizationSlug, input.serviceSlug, date)
      )
    )
      .then((responses) => {
        if (!active) return;
        const seen = new Set<string>();
        const slots = responses
          .flatMap((response) => response.slots)
          .filter(
            (slot) =>
              localDateInZone(
                new Date(slot.slotStart).getTime(),
                input.visitorZone
              ) === input.localDate
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
      .catch(() => {
        if (active) {
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
  }, [input.localDate, input.organizationSlug, input.serviceSlug, input.visitorZone, requestKey, scheduleDates]);

  return {
    slots: state.key === requestKey ? state.slots : [],
    // Loading derives from the request identity so stale responses cannot clear it.
    loading: requestKey !== null && state.key !== requestKey,
    error: state.key === requestKey ? state.error : null,
    refresh: () => setRefreshCount((current) => current + 1),
  };
}
