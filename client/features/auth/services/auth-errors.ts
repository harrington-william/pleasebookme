import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import type { ApiError } from "@/lib/api-error";

export { ApiRequestError as AuthRequestError } from "@/lib/api-error";
export type { ApiError as AuthError } from "@/lib/api-error";

export function normalizeAuthError(error: unknown): ApiError {
  return normalizeApiError(error, {
    statusOverrides: {
      401: "Incorrect username or password.",
      403: "Incorrect username or password.",
      404: "Account not exist.",
      409: "Those details are already registered.",
    },
  });
}

export function normalizeGoogleSignInError(error: unknown): ApiError {
  return normalizeApiError(error);
}

export { normalizeApiError, ApiRequestError };
