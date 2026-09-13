"use client";

import { useFormContext } from "react-hook-form";

import {
  Field,
  inputClassName,
} from "@/features/services/components/service-form-primitives";
import { WidgetCredentialPanel } from "@/features/widgets/components/widget-credential-panel";
import { WidgetStepSection } from "@/features/widgets/components/widget-step-section";
import {
  ORIGIN_PLACEHOLDER,
  type WidgetFormValues,
} from "@/features/widgets/schemas/widget-schema";

type WidgetSecurityStepProps =
  | { mode: "create" }
  | { mode: "edit"; storedPublicKey: string };

export function WidgetSecurityStep(props: WidgetSecurityStepProps) {
  const {
    register,
    formState: { errors },
  } = useFormContext<WidgetFormValues>();

  const credentialsError =
    errors.credentials?.message ??
    errors.credentials?.publicKey?.message ??
    errors.credentials?.secretKey?.message;

  return (
    <WidgetStepSection
      id="security"
      eyebrow="Step 02 · Security"
      title="Security"
    >

      {/* Origin */}
      <Field
        label="Allowed origin"
        htmlFor="origin"
        hint={`The website that may embed this widget, e.g. ${ORIGIN_PLACEHOLDER}. Leave blank to allow any site.`}
        error={errors.origin?.message}
      >
        <input
          id="origin"
          type="text"
          inputMode="url"
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          placeholder={ORIGIN_PLACEHOLDER}
          aria-invalid={errors.origin ? true : undefined}
          className={inputClassName}
          {...register("origin")}
        />
      </Field>

      {/* Key pair */}
      <Field label="Key pair" error={credentialsError}>
        {props.mode === "edit" ? (
          <WidgetCredentialPanel
            mode="edit"
            storedPublicKey={props.storedPublicKey}
          />
        ) : (
          <WidgetCredentialPanel mode="create" />
        )}
      </Field>
    </WidgetStepSection>
  );
}
