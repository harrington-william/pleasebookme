import type { Metadata } from "next";
import Link from "next/link";

import { APP_NAME, AuthCard } from "@/features/auth/components/auth-card";
import { GoogleHandoffExchange } from "@/features/auth/components/google-handoff-exchange";

export const metadata: Metadata = {
  title: "Finishing sign-up",
  description: "Completing your PleaseBookMe account setup with Google.",
  robots: { index: false, follow: false },
};

export default async function GoogleCompletePage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const params = await searchParams;

  const outcome = firstValue(params.google);
  const handoff = firstValue(params.handoff);

  if (outcome === "connected" && handoff) {
    return (
      <AuthCard title={APP_NAME} subtitle="Setting up your workspace">
        <GoogleHandoffExchange code={handoff} />
      </AuthCard>
    );
  }

  if (outcome === "denied") {
    return (
      <OutcomeCard
        subtitle="Sign-up cancelled"
        message="You cancelled before granting access, so no account was created. You can try again whenever you are ready."
      />
    );
  }

  return (
    <OutcomeCard
      subtitle="Sign-up could not be completed"
      message="That sign-up link has expired or was already used. Please start again."
    />
  );
}

function OutcomeCard({
  subtitle,
  message,
}: {
  subtitle: string;
  message: string;
}) {
  return (
    <AuthCard
      title={APP_NAME}
      subtitle={subtitle}
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
      <div className="space-y-md">
        <p className="text-body-md text-muted-foreground">{message}</p>

        <Link
          href="/register"
          className="flex w-full items-center justify-center rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-surface focus-visible:outline-none"
        >
          Back to sign up
        </Link>
      </div>
    </AuthCard>
  );
}

function firstValue(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}
