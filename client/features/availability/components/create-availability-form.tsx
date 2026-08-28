"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { WeekdayRow } from "@/features/availability/components/weekday-row";
import {
  createAvailabilityFormSchema,
  DEFAULT_DAY_VALUES,
  toAvailabilityWindows,
  toCreateAvailabilityRulesetInput,
  type CreateAvailabilityFormValues,
} from "@/features/availability/schemas/availability-schema";
import { createAvailabilityRuleset } from "@/features/availability/services/availability-api";
import { DAY_DEFINITIONS } from "@/features/availability/types/availability";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

const FALLBACK_TIMEZONES = [
  "Asia/Ho_Chi_Minh",
  "Australia/Sydney",
  "UTC",
  "America/New_York",
  "America/Los_Angeles",
  "Europe/London",
];

function supportedTimezones(): string[] {
  try {
    const zones = Intl.supportedValuesOf?.("timeZone");
    return zones && zones.length > 0 ? zones : FALLBACK_TIMEZONES;
  } catch {
    return FALLBACK_TIMEZONES;
  }
}

export function CreateAvailabilityForm() {
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const timezones = useMemo(() => supportedTimezones(), []);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<CreateAvailabilityFormValues>({
    resolver: zodResolver(createAvailabilityFormSchema),
    mode: "onBlur",
    defaultValues: {
      title: "",
      timezone: "Asia/Ho_Chi_Minh",
      days: DEFAULT_DAY_VALUES,
    },
  });

  async function onSubmit(values: CreateAvailabilityFormValues) {
    setSubmitError(null);

    if (toAvailabilityWindows(values).length === 0) {
      setSubmitError("Enable at least one day before saving.");
      return;
    }

    try {
      await createAvailabilityRuleset(toCreateAvailabilityRulesetInput(values));
      router.push("/dashboard/availability");
      router.refresh();
    } catch (error) {
      setSubmitError(
        error instanceof ApiRequestError
          ? error.message
          : "Could not create this availability ruleset. Please try again."
      );
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="grid grid-cols-1 gap-lg lg:grid-cols-3"
    >
      <div className="flex flex-col gap-lg lg:col-span-2">
        {submitError ? <AuthFormAlert message={submitError} /> : null}

        <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
          <h2 className="text-headline-md text-foreground">Basic Details</h2>

          <div className="space-y-base">
            <Label
              htmlFor="title"
              className="text-label-md tracking-wider text-muted-foreground uppercase"
            >
              Ruleset Name
            </Label>
            <Input
              id="title"
              placeholder="e.g., Standard Business Hours"
              aria-invalid={errors.title ? true : undefined}
              className="h-auto rounded-lg border-border bg-background px-md py-sm text-body-md"
              {...register("title")}
            />
            {errors.title ? (
              <p className="text-label-md text-destructive">
                {errors.title.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-base">
            <Label
              htmlFor="timezone"
              className="text-label-md tracking-wider text-muted-foreground uppercase"
            >
              Timezone
            </Label>
            <select
              id="timezone"
              aria-invalid={errors.timezone ? true : undefined}
              className="h-9 w-full rounded-lg border border-border bg-background px-md text-body-md text-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
              {...register("timezone")}
            >
              {timezones.map((zone) => (
                <option key={zone} value={zone}>
                  {zone}
                </option>
              ))}
            </select>
            {errors.timezone ? (
              <p className="text-label-md text-destructive">
                {errors.timezone.message}
              </p>
            ) : null}
          </div>
        </section>

        <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
          <h2 className="text-headline-md text-foreground">Weekly Schedule</h2>

          <div>
            {DAY_DEFINITIONS.map((day, index) => (
              <WeekdayRow
                key={day.value}
                index={index}
                label={day.label}
                control={control}
                register={register}
                errors={errors}
              />
            ))}
          </div>
        </section>
      </div>

      <div className="flex flex-col gap-lg">
        <section className="space-y-sm rounded-xl border border-dashed border-border bg-surface/60 p-lg">
          <h2 className="text-headline-md text-muted-foreground">
            Advanced Rules
          </h2>
          <p className="text-body-md text-muted-foreground">
            Buffers, minimum notice and booking windows live on a{" "}
            <span className="font-mono text-mono-label">Service</span>&apos;s
            booking policy on this platform (
            <span className="font-mono text-mono-label">
              BookingPolicyEntity
            </span>
            ), not on a schedule or availability. There is nothing on{" "}
            <span className="font-mono text-mono-label">core.schedules</span>{" "}
            or{" "}
            <span className="font-mono text-mono-label">
              core.availabilities
            </span>{" "}
            for this panel to configure, so it has been left out rather than
            faked.
          </p>
        </section>

        <section className="space-y-sm rounded-xl border border-dashed border-border bg-surface/60 p-lg">
          <h2 className="text-headline-md text-muted-foreground">
            Date Overrides
          </h2>
          <p className="text-body-md text-muted-foreground">
            The platform has no per-date availability exception model.{" "}
            <span className="font-mono text-mono-label">
              core.out_of_office
            </span>{" "}
            exists, but models booking delegation while a user is away, not a
            &quot;different hours on this date&quot; override — so this panel
            is reserved until a real override entity exists.
          </p>
        </section>

        <button
          type="submit"
          disabled={isSubmitting}
          className={cn(
            "flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors",
            "hover:bg-primary/90",
            "disabled:cursor-not-allowed disabled:opacity-70"
          )}
        >
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              Saving…
            </>
          ) : (
            "Save Availability"
          )}
        </button>
      </div>
    </form>
  );
}
