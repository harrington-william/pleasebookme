import type { Metadata } from "next";

import { LoginForm } from "@/features/auth/components/login-form";
import {
  SESSION_EXPIRED_PARAM,
  SESSION_EXPIRED_VALUE,
} from "@/lib/auth-cookies";

export const metadata: Metadata = {
  title: "Sign In",
  description: "Sign in to your PleaseBookMe workspace.",
};

/**
 * `?session=expired` means a server-side guard ended the session and sent the
 * visitor here; proxy.ts has already cleared the cookies by the time this
 * renders. Explaining that is the whole reason the marker is visible in the
 * URL — being silently signed out with no explanation reads as a bug.
 *
 * searchParams is a Promise in Next.js 16.
 */
export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const params = await searchParams;
  const sessionEnded = params[SESSION_EXPIRED_PARAM] === SESSION_EXPIRED_VALUE;

  return (
    <LoginForm
      notice={
        sessionEnded ? "Your session has ended. Please sign in again." : undefined
      }
    />
  );
}
