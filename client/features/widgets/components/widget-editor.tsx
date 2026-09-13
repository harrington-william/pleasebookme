"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useMemo, useState, type KeyboardEvent } from "react";
import { FormProvider, useForm, useWatch, type FieldErrors } from "react-hook-form";

import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { WidgetBasicsStep } from "@/features/widgets/components/widget-basics-step";
import {
  fieldsForStep,
  nextWidgetStep,
  previousWidgetStep,
  resolveWidgetStep,
  stepForField,
  WIDGET_EDITOR_STEPS,
  WIDGET_STEP_QUERY_KEY,
  type WidgetStepId,
} from "@/features/widgets/components/widget-editor-navigation";
import { WidgetEditorShell } from "@/features/widgets/components/widget-editor-shell";
import { WidgetReviewStep } from "@/features/widgets/components/widget-review-step";
import { WidgetSecurityStep } from "@/features/widgets/components/widget-security-step";
import {
  createWidgetFormSchema,
  DEFAULT_WIDGET_FORM_VALUES,
  toWidgetCreatePayload,
  toWidgetUpdatePayload,
  widgetFormSchema,
  type WidgetFormValues,
} from "@/features/widgets/schemas/widget-schema";
import { createWidget, updateWidget } from "@/features/widgets/services/widget-api";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

const WIDGETS_PATH = "/dashboard/widgets";

const primaryButtonClassName =
  "inline-flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-70";

const ghostButtonClassName =
  "inline-flex items-center justify-center gap-xs rounded-lg border border-border px-md py-sm text-label-md text-muted-foreground transition-colors hover:bg-surface-hover hover:text-foreground";

/**
 * Every step stays mounted; only the active one is displayed. Unmounting would
 * drop the generated key pair along with anything else typed, and hiding keeps
 * each panel's scroll position for free.
 */
function StepPanel({
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

type WidgetEditorProps =
  | { mode: "create" }
  | {
      mode: "edit";
      widgetId: number;
      publicKey: string;
      defaultValues: WidgetFormValues;
    };

export function WidgetEditor(props: WidgetEditorProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const activeStep = resolveWidgetStep(searchParams.get(WIDGET_STEP_QUERY_KEY));
  const schema =
    props.mode === "create" ? createWidgetFormSchema : widgetFormSchema;

  const methods = useForm<WidgetFormValues>({
    resolver: zodResolver(schema),
    mode: "onBlur",
    defaultValues:
      props.mode === "edit" ? props.defaultValues : DEFAULT_WIDGET_FORM_VALUES,
  });

  const {
    control,
    handleSubmit,
    trigger,
    setFocus,
    getFieldState,
    formState: { isSubmitting },
  } = methods;

  const values = useWatch({ control });
  const hasCredentials = Boolean(values.credentials);

  // A step is "complete" when its fields would pass validation right now —
  // derived, never stored, so it cannot drift from what Continue and Save
  // actually check.
  const completedSteps = useMemo(() => {
    const parsed = schema.safeParse(values);
    const invalidFields = new Set(
      parsed.success ? [] : parsed.error.issues.map((issue) => String(issue.path[0]))
    );

    const complete = new Set<WidgetStepId>();
    for (const step of WIDGET_EDITOR_STEPS) {
      if (step.fields.length === 0) continue;
      if (!step.fields.some((field) => invalidFields.has(field))) {
        complete.add(step.id);
      }
    }
    return complete;
  }, [schema, values]);

  function goToStep(step: WidgetStepId) {
    router.replace(`${pathname}?${WIDGET_STEP_QUERY_KEY}=${step}`, {
      scroll: false,
    });
  }

  async function onContinue() {
    const fields = fieldsForStep(activeStep);
    const valid = await trigger(fields);

    if (!valid) {
      const firstInvalid = fields.find((field) => getFieldState(field).invalid);
      // `credentials` has no registered input to focus; its error renders inline.
      if (firstInvalid && firstInvalid !== "credentials") setFocus(firstInvalid);
      return;
    }

    const next = nextWidgetStep(activeStep);
    if (next) goToStep(next);
  }

  function onBack() {
    const previous = previousWidgetStep(activeStep);
    if (previous) goToStep(previous);
  }

  // Enter in a text field should advance, not submit, while the wizard is
  // still on an input step.
  function onKeyDown(event: KeyboardEvent<HTMLFormElement>) {
    if (event.key !== "Enter" || activeStep === "review") return;
    if (!(event.target instanceof HTMLInputElement)) return;
    if (event.target.type !== "text") return;

    event.preventDefault();
    void onContinue();
  }

  async function onSubmit(formValues: WidgetFormValues) {
    setSubmitError(null);

    try {
      if (props.mode === "edit") {
        await updateWidget(props.widgetId, toWidgetUpdatePayload(formValues));
      } else {
        if (formValues.credentials === null) {
          goToStep("security");
          return;
        }
        await createWidget(
          toWidgetCreatePayload({
            ...formValues,
            credentials: formValues.credentials,
          })
        );
      }

      router.push(WIDGETS_PATH);
      router.refresh();
    } catch (error) {
      setSubmitError(
        error instanceof ApiRequestError
          ? error.message
          : "Could not save this widget. Please try again."
      );
    }
  }

  // An invalid field on a hidden step would otherwise fail the submit with
  // nothing on screen to explain why.
  function onInvalid(errors: FieldErrors<WidgetFormValues>) {
    const names = Object.keys(errors) as (keyof WidgetFormValues)[];

    for (const name of names) {
      const step = stepForField(name);

      if (step) {
        if (step !== activeStep) goToStep(step);
        return;
      }
    }
  }

  const saveDisabled = isSubmitting || (props.mode === "create" && !hasCredentials);

  return (
    <FormProvider {...methods}>
      <WidgetEditorShell
        title={props.mode === "edit" ? "Edit Widget" : "Create Widget"}
        description={
          props.mode === "edit"
            ? "Update this widget's details, origin and keys."
            : "Set up an embeddable booking widget in three steps."
        }
        activeStep={activeStep}
        completedSteps={completedSteps}
      >
        <form
          onSubmit={handleSubmit(onSubmit, onInvalid)}
          onKeyDown={onKeyDown}
          noValidate
          className="flex w-full flex-col gap-lg"
        >
          {submitError ? <AuthFormAlert message={submitError} /> : null}

          <StepPanel active={activeStep === "basics"}>
            <WidgetBasicsStep mode={props.mode} />
          </StepPanel>

          <StepPanel active={activeStep === "security"}>
            {props.mode === "edit" ? (
              <WidgetSecurityStep mode="edit" storedPublicKey={props.publicKey} />
            ) : (
              <WidgetSecurityStep mode="create" />
            )}
          </StepPanel>

          <StepPanel active={activeStep === "review"}>
            {props.mode === "edit" ? (
              <WidgetReviewStep mode="edit" storedPublicKey={props.publicKey} />
            ) : (
              <WidgetReviewStep mode="create" />
            )}
          </StepPanel>

          <div className="flex flex-wrap items-center justify-between gap-sm">
            <div>
              {activeStep !== "basics" ? (
                <button
                  type="button"
                  onClick={onBack}
                  className={ghostButtonClassName}
                >
                  Back
                </button>
              ) : null}
            </div>

            <div className="flex items-center gap-sm">
              <Link
                href={WIDGETS_PATH}
                className="rounded-lg border border-border px-md py-sm text-label-md text-destructive transition-colors hover:bg-surface-hover"
              >
                Cancel
              </Link>

              {activeStep === "review" ? (
                <button
                  type="submit"
                  disabled={saveDisabled}
                  className={primaryButtonClassName}
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
                    "Save Widget"
                  )}
                </button>
              ) : (
                <button
                  type="button"
                  onClick={onContinue}
                  className={primaryButtonClassName}
                >
                  Continue
                </button>
              )}
            </div>
          </div>
        </form>
      </WidgetEditorShell>
    </FormProvider>
  );
}
