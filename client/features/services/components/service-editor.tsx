"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { FormProvider, useForm, useWatch, type FieldErrors } from "react-hook-form";

import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import type { Schedule } from "@/features/availability/types/availability";
import { ServiceAvailabilityPanel } from "@/features/services/components/service-availability-panel";
import { ServiceBasicsPanel } from "@/features/services/components/service-basics-panel";
import { ServiceConfirmationPanel } from "@/features/services/components/service-confirmation-panel";
import {
  ServiceEditorContext,
  type ServiceEditorContextValue,
} from "@/features/services/components/service-editor-context";
import {
  resolveServiceTab,
  SERVICE_TAB_QUERY_KEY,
  tabForField,
  type ServiceTabId,
} from "@/features/services/components/service-editor-navigation";
import { ServiceEditorShell } from "@/features/services/components/service-editor-shell";
import { ServiceLimitsPanel } from "@/features/services/components/service-limits-panel";
import { ServicePricePanel } from "@/features/services/components/service-price-panel";
import {
  DEFAULT_SERVICE_FORM_VALUES,
  serviceFormSchema,
  toServicePayload,
  type ServiceFormValues,
} from "@/features/services/schemas/service-schema";
import {
  createService,
  updateService,
} from "@/features/services/services/service-api";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

const FALLBACK_TIMEZONES = [
  "Asia/Saigon",
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

/**
 * Every panel stays mounted; only the active one is displayed. Unmounting the
 * inactive panels would drop whatever the user typed there before saving, and
 * hiding also preserves each panel's scroll position and DOM state.
 */
function TabPanel({
  active,
  children,
}: {
  active: boolean;
  children: React.ReactNode;
}) {
  return (
    <div className={cn("flex-col gap-lg", active ? "flex" : "hidden")}>
      {children}
    </div>
  );
}

type ServiceEditorProps = {
  schedules: Schedule[];
} & (
  | { mode: "create" }
  | { mode: "edit"; serviceId: number; defaultValues: ServiceFormValues }
);

export function ServiceEditor(props: ServiceEditorProps) {
  const { schedules } = props;
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const timezones = useMemo(() => supportedTimezones(), []);
  const [timezoneTouched, setTimezoneTouched] = useState(props.mode === "edit");

  const activeTab = resolveServiceTab(searchParams.get(SERVICE_TAB_QUERY_KEY));

  const methods = useForm<ServiceFormValues>({
    resolver: zodResolver(serviceFormSchema),
    mode: "onBlur",
    defaultValues:
      props.mode === "edit" ? props.defaultValues : DEFAULT_SERVICE_FORM_VALUES,
  });

  const {
    control,
    handleSubmit,
    setValue,
    formState: { isSubmitting },
  } = methods;

  const selectedScheduleId = useWatch({ control, name: "scheduleId" });

  useEffect(() => {
    if (timezoneTouched) return;

    const schedule = schedules.find(
      (candidate) => String(candidate.scheduleId) === selectedScheduleId
    );

    const next =
      schedule?.timezone ?? Intl.DateTimeFormat().resolvedOptions().timeZone;

    if (next && timezones.includes(next)) {
      setValue("timezone", next);
    }
  }, [schedules, selectedScheduleId, setValue, timezones, timezoneTouched]);

  const editorValue = useMemo<ServiceEditorContextValue>(
    () => ({
      mode: props.mode,
      schedules,
      timezones,
      onTimezoneTouched: () => setTimezoneTouched(true),
    }),
    [props.mode, schedules, timezones]
  );

  function goToTab(tab: ServiceTabId) {
    router.replace(`${pathname}?${SERVICE_TAB_QUERY_KEY}=${tab}`, {
      scroll: false,
    });
  }

  async function onSubmit(values: ServiceFormValues) {
    setSubmitError(null);

    const payload = toServicePayload(values);

    try {
      if (props.mode === "edit") {
        await updateService(props.serviceId, payload);
      } else {
        await createService(payload);
      }
      router.push("/dashboard/services");
      router.refresh();
    } catch (error) {
      setSubmitError(
        error instanceof ApiRequestError
          ? error.message
          : `Could not ${props.mode === "edit" ? "update" : "create"} this service. Please try again.`
      );
    }
  }

  // An invalid field on a hidden tab would otherwise fail the submit with
  // nothing on screen to explain why.
  function onInvalid(errors: FieldErrors<ServiceFormValues>) {
    const names = Object.keys(errors) as (keyof ServiceFormValues)[];

    for (const name of names) {
      const tab = tabForField(name);

      if (tab) {
        if (tab !== activeTab) goToTab(tab);
        return;
      }
    }
  }

  return (
    <ServiceEditorContext.Provider value={editorValue}>
      <FormProvider {...methods}>
        <ServiceEditorShell
          activeTab={activeTab}
          title={props.mode === "edit" ? "Edit Service" : "Create New Service"}
          description={
            props.mode === "edit"
              ? "Update the details, pricing and booking rules for this service."
              : "Configure details, pricing, and availability rules."
          }
        >
          <form
            onSubmit={handleSubmit(onSubmit, onInvalid)}
            noValidate
            className="flex w-full max-w-3xl flex-col gap-lg"
          >
            {submitError ? <AuthFormAlert message={submitError} /> : null}

            <TabPanel active={activeTab === "basics"}>
              <ServiceBasicsPanel />
            </TabPanel>

            <TabPanel active={activeTab === "availability"}>
              <ServiceAvailabilityPanel />
            </TabPanel>

            <TabPanel active={activeTab === "price"}>
              <ServicePricePanel />
            </TabPanel>

            <TabPanel active={activeTab === "limits"}>
              <ServiceLimitsPanel />
            </TabPanel>

            <TabPanel active={activeTab === "confirmation"}>
              <ServiceConfirmationPanel />
            </TabPanel>

            <div className="flex items-center gap-sm">
              <button
                type="submit"
                disabled={isSubmitting || schedules.length === 0}
                className={cn(
                  "flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors",
                  "hover:bg-primary/90",
                  "disabled:cursor-not-allowed disabled:opacity-70"
                )}
              >
                {isSubmitting ? (
                  <>
                    <LoaderCircle
                      className="size-4 animate-spin"
                      aria-hidden="true"
                    />
                    Saving…
                  </>
                ) : props.mode === "edit" ? (
                  "Save Changes"
                ) : (
                  "Save Service"
                )}
              </button>

              <Link
                href="/dashboard/services"
                className="rounded-lg border border-border px-md py-sm text-label-md text-destructive transition-colors hover:bg-surface-hover"
              >
                Cancel
              </Link>
            </div>
          </form>
        </ServiceEditorShell>
      </FormProvider>
    </ServiceEditorContext.Provider>
  );
}
