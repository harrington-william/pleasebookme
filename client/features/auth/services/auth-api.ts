import type {
  LoginFormValues,
  RegisterFormValues,
} from "@/features/auth/schemas/auth-schema";
import { toRegisterRequest } from "@/features/auth/schemas/auth-schema";
import {
  AuthRequestError,
  normalizeAuthError,
  normalizeGoogleSignInError,
} from "@/features/auth/services/auth-errors";
import type { SessionActor } from "@/features/auth/types/auth";
import { bffClient } from "@/lib/axios";

/**
 * Browser-side auth operations.
 *
 * These call our OWN Next.js route handlers (/api/auth/*), never the Spring
 * Boot platform directly. That indirection is what lets the session live in
 * httpOnly cookies: the tokens are set server-side and this code never sees
 * them.
 *
 * Every function throws AuthRequestError on failure, carrying an already
 * renderable message, so form components can `catch` and display without
 * knowing anything about axios or the platform's error shapes.
 */

/** Identity echoed back by the BFF. Contains no tokens, by design. */
type SessionSummary = Pick<
  SessionActor,
  "subject" | "actorType" | "tenantUid"
>;

export async function registerAccount(
  values: RegisterFormValues
): Promise<SessionSummary> {
  try {
    const response = await bffClient.post<SessionSummary>(
      "/auth/register",
      toRegisterRequest(values)
    );
    return response.data;
  } catch (error) {
    throw new AuthRequestError(normalizeAuthError(error));
  }
}

export async function loginWithCredentials(
  values: LoginFormValues
): Promise<SessionSummary> {
  try {
    const response = await bffClient.post<SessionSummary>("/auth/login", {
      username: values.username,
      password: values.password,
    });
    return response.data;
  } catch (error) {
    throw new AuthRequestError(normalizeAuthError(error));
  }
}

/**
 * Exchanges a Google ID token for a platform session.
 *
 * `idToken` comes from Google Identity Services in the browser (see
 * use-google-identity.ts). It is forwarded straight to the BFF and never
 * stored client-side — the resulting platform session lands in httpOnly
 * cookies like any other sign-in.
 */
export async function signInWithGoogle(
  idToken: string
): Promise<SessionSummary> {
  try {
    const response = await bffClient.post<SessionSummary>("/auth/google", {
      idToken,
    });
    return response.data;
  } catch (error) {
    throw new AuthRequestError(normalizeGoogleSignInError(error));
  }
}

export async function logout(): Promise<void> {
  try {
    await bffClient.post("/auth/logout");
  } catch (error) {
    throw new AuthRequestError(normalizeAuthError(error));
  }
}
