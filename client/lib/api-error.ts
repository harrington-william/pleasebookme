import axios from "axios";

export interface PlatformErrorResponse {
  status: string;
  message: string;
  data: unknown;
  client: string;
  timestamp: string;
  path: string;
}

export interface ApiError {
  message: string;
  status: number;
  fieldErrors?: Record<string, string>;
}

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
