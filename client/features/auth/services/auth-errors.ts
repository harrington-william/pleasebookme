import axios from "axios";

import type { AuthError, PlatformErrorResponse } from "@/features/auth/types/auth";

/**
 * Error normalization.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * KNOWN BACKEND GAP — revisit once the server is fixed.
 *
 * The platform can currently return TWO different error shapes:
 *
 *   1. ApiErrorResponse { status, message, data, client, timestamp, path }
 *      — emitted by GlobalExceptionHandler for every typed domain exception
 *        (duplicate username/email/phone → 409, not found → 404, expired or
 *        revoked token → 403, ...).
 *
 *   2. Spring Boot's DEFAULT error envelope
 *      — because GlobalExceptionHandler registers no @ExceptionHandler for
 *        MethodArgumentNotValidException (@Valid failures) or for Spring
 *        Security's BadCredentialsException / AuthenticationException. A wrong
 *        password on /login therefore does NOT produce ApiErrorResponse, and
 *        may even have an empty body since no AuthenticationEntryPoint is
 *        configured.
 *
 * As agreed, this normalizer treats shape (1) as the contract and synthesizes a
 * sensible message for the cases that fall through to (2) — most importantly
 * bad credentials, where the status code is the only reliable signal. When the
 * backend registers the missing handlers, delete the status-based fallbacks
 * below and read `message` unconditionally.
 * ─────────────────────────────────────────────────────────────────────────────
 */

/** Thrown by the browser-side auth services so callers can `catch` normally. */
export class AuthRequestError extends Error {
  readonly detail: AuthError;

  constructor(detail: AuthError) {
    super(detail.message);
    this.name = "AuthRequestError";
    this.detail = detail;
  }
}

function isPlatformErrorResponse(body: unknown): body is PlatformErrorResponse {
  return (
    typeof body === "object" &&
    body !== null &&
    typeof (body as PlatformErrorResponse).message === "string" &&
    (body as PlatformErrorResponse).message.length > 0
  );
}

/**
 * Message of last resort, chosen from the status code alone.
 *
 * This exists only because of the backend gap described above. `401`/`403` on
 * an auth endpoint overwhelmingly means "wrong credentials" today, but note
 * that the platform also maps token-expired/revoked to 403 — so this string is
 * a best guess, not a faithful translation of the server's intent.
 */
function messageForStatus(status: number): string {
  switch (status) {
    case 400:
      return "Some of the details you entered are not valid. Please check and try again.";
    case 401:
    case 403:
      return "Incorrect username or password.";
    case 404:
      return "We could not find an account matching those details.";
    case 409:
      return "Those details are already registered.";
    case 429:
      return "Too many attempts. Please wait a moment and try again.";
    default:
      return status >= 500
        ? "The platform is unavailable right now. Please try again shortly."
        : "Something went wrong. Please try again.";
  }
}

/**
 * Collapses anything thrown by axios (or by our own route handlers) into one
 * renderable shape.
 */
export function normalizeAuthError(error: unknown): AuthError {
  if (axios.isAxiosError(error)) {
    // No response at all: DNS failure, connection refused, timeout, offline.
    if (!error.response) {
      return {
        message:
          error.code === "ECONNABORTED"
            ? "The request timed out. Please try again."
            : "Could not reach the server. Check your connection and try again.",
        status: 0,
      };
    }

    const { status, data } = error.response;

    // Our own BFF route handlers already return the normalized shape.
    if (
      typeof data === "object" &&
      data !== null &&
      typeof (data as AuthError).message === "string" &&
      typeof (data as AuthError).status === "number"
    ) {
      return data as AuthError;
    }

    // Shape (1): the platform's ApiErrorResponse. Trust its message — except
    // on 401/403, where the body is usually empty or unhelpful.
    if (isPlatformErrorResponse(data) && status !== 401 && status !== 403) {
      return { message: data.message, status };
    }

    // Shape (2): Spring Boot's default envelope, or no body at all.
    return { message: messageForStatus(status), status };
  }

  if (error instanceof AuthRequestError) return error.detail;

  return {
    message: "Something went wrong. Please try again.",
    status: 0,
  };
}
