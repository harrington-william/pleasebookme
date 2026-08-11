"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";

import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import {
  APP_NAME,
  AuthCard,
  AuthDivider,
} from "@/features/auth/components/auth-card";
import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { GoogleOnboardingButton } from "@/features/auth/components/google-onboarding-button";
import { AuthSubmitButton } from "@/features/auth/components/auth-submit-button";
import {
  registerFormSchema,
  type RegisterFormValues,
} from "@/features/auth/schemas/auth-schema";
import { registerAccount } from "@/features/auth/services/auth-api";
import { AuthRequestError } from "@/features/auth/services/auth-errors";

export function RegisterForm() {
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerFormSchema),
    criteriaMode: "all",
    mode: "onBlur",
    defaultValues: {
      firstName: "",
      lastName: "",
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
      terms: false,
    },
  });

  async function onSubmit(values: RegisterFormValues) {
    setSubmitError(null);

    try {
      await registerAccount(values);
      
      // Redirect to dashboard
      router.push("/dashboard");
      router.refresh();
    } catch (error) {
      setSubmitError(
        error instanceof AuthRequestError
          ? error.message
          : "Something went wrong. Please try again."
      );
    }
  }

  const passwordRules = errors.password?.types
    ? Object.values(errors.password.types).flat().filter(Boolean)
    : null;

  return (
    <AuthCard
      title={APP_NAME}
      subtitle="Enterprise Infrastructure Initialization"
      footer={
        <p className="text-body-md text-muted-foreground">
          Already have an account?{" "}
          <Link
            href="/login"
            className="text-label-md text-primary transition-all hover:underline"
          >
            Sign In
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-md">
        {submitError ? <AuthFormAlert message={submitError} /> : null}

        <div className="grid grid-cols-1 gap-md md:grid-cols-2">
          <AuthField
            id="firstName"
            label="First Name"
            placeholder="Ada"
            autoComplete="given-name"
            error={errors.firstName?.message}
            {...register("firstName")}
          />
          <AuthField
            id="lastName"
            label="Last Name"
            placeholder="Lovelace"
            autoComplete="family-name"
            error={errors.lastName?.message}
            {...register("lastName")}
          />
        </div>

        <AuthField
          id="username"
          label="Username"
          placeholder="ada.lovelace"
          autoComplete="username"
          error={errors.username?.message}
          {...register("username")}
        />

        <AuthField
          id="email"
          label="Business Email"
          type="email"
          placeholder="ada@enterprise.io"
          autoComplete="email"
          error={errors.email?.message}
          {...register("email")}
        />

        <AuthField
          id="password"
          label="Password"
          type="password"
          placeholder="••••••••"
          autoComplete="new-password"
          error={
            passwordRules && passwordRules.length > 0 ? (
              <>
                <p>Password must:</p>
                <ul className="mt-base list-disc space-y-base pl-md">
                  {passwordRules.map((rule) => (
                    <li key={String(rule)}>{rule}</li>
                  ))}
                </ul>
              </>
            ) : null
          }
          {...register("password")}
        />

        <AuthField
          id="confirmPassword"
          label="Confirm Password"
          type="password"
          placeholder="••••••••"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />

        <div className="pt-sm">
          <div className="flex items-start gap-sm">
            <Controller
              control={control}
              name="terms"
              render={({ field }) => (
                <Checkbox
                  id="terms"
                  name={field.name}
                  inputRef={field.ref}
                  checked={field.value}
                  onCheckedChange={(checked) => field.onChange(checked)}
                  onBlur={field.onBlur}
                  aria-invalid={errors.terms ? true : undefined}
                  className="mt-[2px]"
                />
              )}
            />
            <Label
              htmlFor="terms"
              className="text-body-md leading-snug text-muted-foreground"
            >
              I accept the{" "}
              <Link href="/terms" className="text-primary hover:underline">
                Terms of Service
              </Link>{" "}
              and{" "}
              <Link href="/privacy" className="text-primary hover:underline">
                Privacy Policy
              </Link>
              .
            </Label>
          </div>

          {errors.terms ? (
            <p className="mt-xs text-label-md text-destructive">
              {errors.terms.message}
            </p>
          ) : null}
        </div>

        <AuthSubmitButton isSubmitting={isSubmitting} className="mt-xl">
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              Creating account…
            </>
          ) : (
            "Create Account"
          )}
        </AuthSubmitButton>
      </form>

      <AuthDivider />

      {/* Consent screen */}
      <GoogleOnboardingButton />
    </AuthCard>
  );
}
