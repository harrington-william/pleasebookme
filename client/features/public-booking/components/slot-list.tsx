import { CloudSun, Moon, Sun } from "lucide-react";

import { zonedParts } from "@/features/public-booking/lib/date-math";
import type { TimeSlot } from "@/features/public-booking/types/public-booking";

import { ErrorMessage } from "./error-message";
import { SlotGroup } from "./slot-group";
import { Spinner } from "./spinner";

export function SlotList({
  slots,
  visitorZone,
  selected,
  onSelect,
  loading,
  error,
}: {
  slots: TimeSlot[];
  visitorZone: string;
  selected: TimeSlot | null;
  onSelect: (slot: TimeSlot) => void;
  loading: boolean;
  error: string | null;
}) {
  if (loading) return <Spinner label="Loading times…" />;
  if (error) return <ErrorMessage message={error} />;
  if (!slots.length) {
    return <p className="py-xl text-center text-body-md text-muted-foreground">No available times for this day.</p>;
  }

  // These boundaries follow the visitor's wall clock, not the schedule zone.
  const groups = {
    morning: slots.filter((slot) => zonedParts(new Date(slot.slotStart).getTime(), visitorZone).hour < 12),
    afternoon: slots.filter((slot) => {
      const hour = zonedParts(new Date(slot.slotStart).getTime(), visitorZone).hour;
      return hour >= 12 && hour < 17;
    }),
    evening: slots.filter((slot) => zonedParts(new Date(slot.slotStart).getTime(), visitorZone).hour >= 17),
  };

  return (
    <div className="flex flex-col gap-xl">
      <SlotGroup title="Morning" icon={Sun} iconClassName="text-warning" slots={groups.morning} visitorZone={visitorZone} selected={selected} onSelect={onSelect} />
      <SlotGroup title="Afternoon" icon={CloudSun} iconClassName="text-warning" slots={groups.afternoon} visitorZone={visitorZone} selected={selected} onSelect={onSelect} />
      <SlotGroup title="Evening" icon={Moon} iconClassName="text-primary" slots={groups.evening} visitorZone={visitorZone} selected={selected} onSelect={onSelect} />
    </div>
  );
}
