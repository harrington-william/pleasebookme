"use client";

import type { ReactNode } from "react";

import { BOOKING_STEPS, stepNumber, type BookingStep } from "../../../logic/steps";
import type { Organization, Service } from "../../../logic/types";

import { BookingFooter } from "./booking-footer";
import { BookingSidebar } from "./booking-sidebar";

export function BookingShell({
  organization,
  service,
  activeStep,
  children,
  onNavigate,
}: {
  organization: Organization;
  service: Service | null;
  activeStep: BookingStep;
  children: ReactNode;
  onNavigate?: (step: BookingStep) => void;
}) {
  const progress = (stepNumber(activeStep) / BOOKING_STEPS.length) * 100;

  return (
    <main className="flex flex-1 items-center justify-center px-md py-lg md:px-2xl md:py-xl">
      <section
        aria-label={`Book with ${organization.name}`}
        className="flex w-full max-w-5xl flex-col overflow-hidden rounded-xl border border-border bg-surface shadow-2xl md:h-[min(800px,calc(100dvh-7rem))] md:min-h-[600px] md:flex-row"
      >
        <BookingSidebar
          organization={organization}
          service={service}
          activeStep={activeStep}
          onNavigate={onNavigate}
        />
        <div data-pbm-scroll className="relative flex min-w-0 flex-1 flex-col md:overflow-y-auto">
          <div className="z-10 h-1 w-full shrink-0 bg-surface-hover md:sticky md:top-0">
            <div
              className="h-full bg-primary transition-[width] duration-500 ease-out"
              style={{ width: `${progress}%` }}
            />
          </div>
          <div className="mx-auto flex w-full max-w-[560px] flex-1 flex-col px-md py-lg md:px-xl md:py-xl">
            {children}
            <BookingFooter />
          </div>
        </div>
      </section>
    </main>
  );
}
