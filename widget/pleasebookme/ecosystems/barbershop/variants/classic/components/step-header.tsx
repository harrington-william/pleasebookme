import type { BookingStep } from "../../../logic/steps";
import { stepNumber } from "../../../logic/steps";

export function StepHeader({
  step,
  title,
  subtitle,
}: {
  step: BookingStep;
  title: string;
  subtitle?: string;
}) {
  return (
    <header className="mb-xl">
      <p className="mb-base text-label-md font-semibold tracking-wider text-primary uppercase">
        Step {stepNumber(step)} of 6
      </p>
      <h1 className="text-headline-lg-mobile md:text-headline-lg">{title}</h1>
      {subtitle ? (
        <p className="mt-xs text-body-lg text-muted-foreground">{subtitle}</p>
      ) : null}
    </header>
  );
}
