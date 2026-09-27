"use client";

import {
  Calendar,
  Check,
  CircleCheck,
  Clock,
  ListChecks,
  MapPin,
  Settings,
  Store,
  UserRound,
} from "lucide-react";

import {
  BOOKING_STEPS,
  stepIndex,
  stepLabel,
  stepNumber,
  type BookingStep,
} from "../../../logic/steps";
import { formatDuration, formatPriceRange } from "../../../../../core/lib/format";
import type { Organization, Service } from "../../../logic/types";
import { cn } from "../../../../../core/lib/cn";

const ICONS = {
  service: Settings,
  date: Calendar,
  time: Clock,
  details: UserRound,
  review: ListChecks,
  done: CircleCheck,
};

function Logo({ url, className }: { url: string | null; className: string }) {
  return (
    <div
      className={cn(
        "flex shrink-0 items-center justify-center overflow-hidden rounded-full border border-border bg-surface-hover",
        className
      )}
    >
      {url ? (
        // The logo is tenant-supplied and can live on any configured CDN.
        // eslint-disable-next-line @next/next/no-img-element
        <img src={url} alt="" className="size-full object-cover" />
      ) : (
        <Store className="size-[40%] text-muted-foreground" aria-hidden="true" />
      )}
    </div>
  );
}

// Organizations carry no address column; a location is only shown on the
// first step when every bookable service agrees on one.
function sharedLocation(organization: Organization): string | null {
  const locations = new Set(
    organization.services
      .map((service) => service.location)
      .filter((location): location is string => Boolean(location))
  );
  return locations.size === 1 ? [...locations][0] : null;
}

export function BookingSidebar({
  organization,
  service,
  activeStep,
  onNavigate,
}: {
  organization: Organization;
  service: Service | null;
  activeStep: BookingStep;
  onNavigate?: (step: BookingStep) => void;
}) {
  const activeIndex = stepIndex(activeStep);
  const showSteps = activeStep !== "service";
  const location = showSteps ? service?.location ?? null : sharedLocation(organization);

  return (
    <aside className="flex shrink-0 flex-col border-b border-border bg-surface-container p-md md:w-72 md:border-r md:border-b-0 md:p-lg">
      {showSteps ? (
        <div
          key="steps"
          className="flex flex-1 animate-pbm-enter flex-col gap-lg duration-300"
        >
          <div className="flex items-center gap-sm">
            <Logo url={organization.logoUrl} className="size-10" />
            <div className="min-w-0">
              <p className="truncate text-body-lg font-semibold">{organization.name}</p>
              <p className="truncate text-label-md text-muted-foreground md:hidden">
                Step {stepNumber(activeStep)} of {BOOKING_STEPS.length} · {stepLabel(activeStep)}
              </p>
            </div>
          </div>

          <nav aria-label="Booking progress" className="hidden flex-col gap-xs md:flex">
            {BOOKING_STEPS.map((item, index) => {
              const Icon = ICONS[item.key];
              const active = item.key === activeStep;
              const done = index < activeIndex;
              const clickable = Boolean(onNavigate) && done && activeStep !== "done";
              return (
                <button
                  key={item.key}
                  type="button"
                  disabled={!clickable}
                  onClick={() => onNavigate?.(item.key)}
                  className={cn(
                    "flex min-h-10 items-center gap-md rounded-lg px-xs py-xs text-body-md text-muted-foreground transition-colors",
                    active && "border-l-2 border-primary bg-surface-hover/40 pl-xs font-bold text-primary",
                    clickable && "cursor-pointer hover:bg-surface-hover hover:text-foreground",
                    !clickable && "cursor-default",
                    item.key === "done" && !active && "opacity-50"
                  )}
                >
                  <Icon className="size-5" aria-hidden="true" />
                  <span>{item.label}</span>
                  {done ? <Check className="ml-auto size-4 text-primary" aria-hidden="true" /> : null}
                </button>
              );
            })}
          </nav>

          {service ? (
            <div className="mt-auto hidden flex-col gap-xs border-t border-border pt-md md:flex">
              <p className="text-label-md font-semibold tracking-wider text-muted-foreground uppercase">
                Your booking
              </p>
              <p className="text-headline-sm">{service.title}</p>
              <p className="flex items-center gap-xs text-body-md text-muted-foreground">
                <Clock className="size-4" aria-hidden="true" />
                {formatDuration(service.durationMinutes)} ·{" "}
                {formatPriceRange(service.minPrice, service.maxPrice, service.currency)}
              </p>
              {location ? (
                <p className="flex items-start gap-xs text-body-md text-muted-foreground">
                  <MapPin className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
                  <span>{location}</span>
                </p>
              ) : null}
            </div>
          ) : null}
        </div>
      ) : (
        <div
          key="organization"
          className="flex animate-pbm-enter items-center gap-md duration-300 md:flex-col md:items-center md:pt-lg md:text-center"
        >
          <Logo url={organization.logoUrl} className="size-16 md:size-24" />
          <div className="min-w-0 md:w-full">
            <p className="text-headline-md font-semibold">{organization.name}</p>
            {organization.bio ? (
              <p className="mt-base line-clamp-3 text-body-md text-muted-foreground">
                {organization.bio}
              </p>
            ) : null}
            {location ? (
              <p className="mt-sm flex items-start gap-xs text-body-md text-muted-foreground md:justify-center">
                <MapPin className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
                <span>{location}</span>
              </p>
            ) : null}
          </div>
        </div>
      )}
    </aside>
  );
}
