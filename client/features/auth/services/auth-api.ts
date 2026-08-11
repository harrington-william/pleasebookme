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
import type {
  GoogleAuthorizeResponse,
  SessionActor,
} from "@/features/auth/types/auth";
import { normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

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

export async function startGoogleOnboarding(): Promise<GoogleAuthorizeResponse> {
  try {
    const response = await bffClient.post<GoogleAuthorizeResponse>(
      "/auth/google/authorize",
      {}
    );
    return response.data;
  } catch (error) {
    throw new AuthRequestError(normalizeApiError(error));
  }
}

export async function exchangeGoogleHandoff(
  code: string
): Promise<SessionSummary> {
  try {
    const response = await bffClient.post<SessionSummary>(
      "/auth/google/handoff",
      { code }
    );
    return response.data;
  } catch (error) {
    throw new AuthRequestError(normalizeApiError(error));
  }
}

export async function logout(): Promise<void> {
  try {
    await bffClient.post("/auth/logout");
  } catch (error) {
    throw new AuthRequestError(normalizeAuthError(error));
  }
}
