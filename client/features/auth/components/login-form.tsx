"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";

import {
  APP_NAME,
  AuthCard,
  AuthDivider,
} from "@/features/auth/components/auth-card";
import { AuthField } from "@/features/auth/components/auth-field";
import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import { AuthSubmitButton } from "@/features/auth/components/auth-submit-button";
import { GoogleAuthButton } from "@/features/auth/components/google-auth-button";
import {
  loginFormSchema,
  type LoginFormValues,
} from "@/features/auth/schemas/auth-schema";
import { loginWithCredentials } from "@/features/auth/services/auth-api";
import { AuthRequestError } from "@/features/auth/services/auth-errors";

/**
 * `notice` is a neutral, server-supplied message shown above the form — used
 * when the visitor did not choose to be here (an expired session bounced them).
 * It is deliberately not styled as an error: nothing went wrong, and it is
 * suppressed once a real submit error exists, which is the more urgent message.
 */
export function LoginForm({ notice }: { notice?: string }) {
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginFormSchema),
    mode: "onBlur",
    defaultValues: { username: "", password: "" },
  });

  async function onSubmit(values: LoginFormValues) {
    setSubmitError(null);

    try {
      await loginWithCredentials(values);
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

  return (
    <AuthCard
      title={APP_NAME}
      subtitle="Sign in to your workspace"
      footer={
        <p className="text-body-md text-muted-foreground">
          Don&apos;t have an account?{" "}
          <Link
            href="/register"
            className="text-label-md text-primary transition-all hover:underline"
          >
            Create one
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-md">
        {submitError ? <AuthFormAlert message={submitError} /> : null}

        {!submitError && notice ? (
          <p
            role="status"
            className="rounded-lg border border-border bg-surface-container px-md py-sm text-body-md text-muted-foreground"
          >
            {notice}
          </p>
        ) : null}

        <AuthField
          id="username"
          label="Username"
          placeholder="ada.lovelace"
          autoComplete="username"
          error={errors.username?.message}
          {...register("username")}
        />

        <AuthField
          id="password"
          label="Password"
          type="password"
          placeholder="••••••••"
          autoComplete="current-password"
          error={errors.password?.message}
          action={
            // Reserved: design/client/stitch/forgot_password_pleasebookme
            // exists, but the platform has no password-reset endpoint yet.
            <Link
              href="/forgot-password"
              className="text-label-md text-primary transition-colors hover:underline"
            >
              Forgot password?
            </Link>
          }
          {...register("password")}
        />

        <AuthSubmitButton isSubmitting={isSubmitting} className="mt-lg">
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              Signing in…
            </>
          ) : (
            "Sign In to Console"
          )}
        </AuthSubmitButton>
      </form>

      <AuthDivider />

      <GoogleAuthButton label="Continue with Google" />
    </AuthCard>
  );
}
