import axios from "axios";

/**
 * Platform error normalization, shared by every feature.
 *
 * Extracted from features/auth/services/auth-errors.ts once a second consumer
 * (the Google integrations feature) appeared — none of this logic was ever
 * auth-specific, it normalizes the platform's generic ApiErrorResponse shape.
 * auth-errors.ts re-exports these for backward compatibility.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * KNOWN BACKEND GAP — the platform can return TWO different error shapes:
 *
 *   1. ApiErrorResponse { status, message, data, client, timestamp, path }
 *      — GlobalExceptionHandler emits this for every typed domain exception.
 *
 *   2. Spring Boot's DEFAULT error envelope
 *      — because GlobalExceptionHandler registers no @ExceptionHandler for
 *        MethodArgumentNotValidException (@Valid failures) or Spring Security's
 *        BadCredentialsException / AuthenticationException. A wrong password on
 *        /login therefore does NOT produce ApiErrorResponse, and may even have
 *        an empty body since no AuthenticationEntryPoint is configured.
 *
 * We treat shape (1) as the contract and synthesize a message for the cases
 * that fall through to (2), where the status code is the only reliable signal.
 * ─────────────────────────────────────────────────────────────────────────────
 */

/** The platform's ApiErrorResponse shape. */
export interface PlatformErrorResponse {
  status: string;
  message: string;
  data: unknown;
  /** Caller's remote address, despite the name. Not a client identifier. */
  client: string;
  timestamp: string;
  path: string;
}

/** Normalized, renderable error. */
export interface ApiError {
  message: string;
  /** HTTP status that produced it, or 0 for network/transport failures. */
  status: number;
  /** Field-scoped messages, when the failure was a validation error. */
  fieldErrors?: Record<string, string>;
}

/** Thrown by browser-side services so callers can `catch` normally. */
export class ApiRequestError extends Error {
  readonly detail: ApiError;

  constructor(detail: ApiError) {
    super(detail.message);
    this.name = "ApiRequestError";
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
 * Only reached for shape (2) above, or when the body is empty. Note the
 * platform maps several distinct conditions onto the same code (403 covers
 * both a wrong actor type and an expired/revoked token), so these strings are
 * a best guess rather than a faithful translation of the server's intent.
 */
function messageForStatus(status: number): string {
  switch (status) {
    case 400:
      return "Some of the details you entered are not valid. Please check and try again.";
    case 401:
      return "Your session has expired. Please sign in again.";
    case 403:
      return "You do not have permission to do that.";
    case 404:
      return "We could not find what you were looking for.";
    case 409:
      return "That conflicts with something that already exists.";
    case 429:
      return "Too many attempts. Please wait a moment and try again.";
    case 502:
      return "The platform could not reach an upstream service. Please try again.";
    default:
      return status >= 500
        ? "The platform is unavailable right now. Please try again shortly."
        : "Something went wrong. Please try again.";
  }
}

/**
 * Collapses anything thrown by axios (or by our own route handlers) into one
 * renderable shape.
 *
 * `trustBodyOn401` exists because the two call sites want opposite behaviour:
 * on a login form a 401 body is unhelpful boilerplate and a friendly
 * "incorrect username or password" is better, whereas on an authenticated
 * feature call a 401 carries real meaning ("session expired") that the caller
 * may want verbatim.
 */
export function normalizeApiError(
  error: unknown,
  options: { statusOverrides?: Partial<Record<number, string>> } = {}
): ApiError {
  const { statusOverrides = {} } = options;

  const withOverride = (status: number, message: string): ApiError => ({
    message: statusOverrides[status] ?? message,
    status,
  });

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
      typeof (data as ApiError).message === "string" &&
      typeof (data as ApiError).status === "number"
    ) {
      return data as ApiError;
    }

    if (statusOverrides[status]) {
      return withOverride(status, messageForStatus(status));
    }

    // Shape (1): trust the platform's own message.
    if (isPlatformErrorResponse(data)) {
      return { message: data.message, status };
    }

    // Shape (2): Spring Boot's default envelope, or no body at all.
    return { message: messageForStatus(status), status };
  }

  if (error instanceof ApiRequestError) return error.detail;

  return {
    message: "Something went wrong. Please try again.",
    status: 0,
  };
}
