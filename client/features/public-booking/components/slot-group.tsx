import { Check, type LucideIcon } from "lucide-react";

import { formatSlotTime } from "@/features/public-booking/lib/date-math";
import type { TimeSlot } from "@/features/public-booking/types/public-booking";
import { cn } from "@/lib/utils";

export function SlotGroup({
  title,
  icon: Icon,
  iconClassName,
  slots,
  visitorZone,
  selected,
  onSelect,
}: {
  title: string;
  icon: LucideIcon;
  iconClassName: string;
  slots: TimeSlot[];
  visitorZone: string;
  selected: TimeSlot | null;
  onSelect: (slot: TimeSlot) => void;
}) {
  if (!slots.length) return null;
  return (
    <section>
      <h2 className="mb-md flex items-center gap-xs text-headline-sm">
        <Icon className={cn("size-5", iconClassName)} aria-hidden="true" />
        {title}
      </h2>
      <div className="grid grid-cols-2 gap-md">
        {slots.map((slot) => {
          const active = selected?.slotStart === slot.slotStart;
          return (
            <button
              key={slot.slotStart}
              type="button"
              onClick={() => onSelect(slot)}
              className={cn(
                "flex h-12 cursor-pointer items-center justify-center gap-xs rounded-full border border-border bg-surface text-body-md font-medium transition-colors hover:border-primary hover:text-primary",
                active && "border-primary bg-primary text-primary-foreground hover:text-primary-foreground"
              )}
            >
              {formatSlotTime(slot.slotStart, visitorZone)}
              {active ? <Check className="size-4" aria-hidden="true" /> : null}
            </button>
          );
        })}
      </div>
    </section>
  );
}
