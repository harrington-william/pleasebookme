import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import type { ApiError } from "@/lib/api-error";

/**
 * Auth-flavoured error normalization.
 *
 * The generic machinery now lives in lib/api-error.ts (it was never
 * auth-specific — it normalizes the platform's ApiErrorResponse shape, which
 * every feature sees). This module keeps the auth-specific wording and the
 * historical names so existing call sites stay unchanged.
 */

/** @deprecated Prefer `ApiRequestError` from lib/api-error. Kept for call-site stability. */
export { ApiRequestError as AuthRequestError } from "@/lib/api-error";
export type { ApiError as AuthError } from "@/lib/api-error";

/**
 * On the login and register screens, a 401/403 means one thing in practice:
 * the credentials were wrong. The platform's body for that case is unhelpful
 * or absent (no AuthenticationEntryPoint is configured), so we override it
 * with wording a user can act on.
 */
export function normalizeAuthError(error: unknown): ApiError {
  return normalizeApiError(error, {
    statusOverrides: {
      401: "Incorrect username or password.",
      403: "Incorrect username or password.",
      404: "We could not find an account matching those details.",
      409: "Those details are already registered.",
    },
  });
}

/**
 * Google Sign-In failures, which behave differently from password login.
 *
 * Here the platform DOES register typed handlers, so its message body is
 * meaningful and should be shown as-is:
 *   InvalidGoogleIdTokenException        → 401
 *   GoogleAccountEmailNotVerifiedException → 409
 *
 * That last one is a real security control, not a formality: the platform
 * refuses to link a Google identity onto an existing account unless Google
 * says the email is verified, otherwise anyone able to create a Google account
 * bearing a victim's address could take over that account. Surface the
 * platform's own explanation rather than a generic credentials message.
 */
export function normalizeGoogleSignInError(error: unknown): ApiError {
  return normalizeApiError(error);
}

/** Re-exported so auth call sites need not import from two modules. */
export { normalizeApiError, ApiRequestError };
