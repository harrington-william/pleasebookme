import type { ApiError } from "@/lib/api-error";

export class PublicPageNotFoundError extends Error {}

export class SlotUnavailableError extends Error {}

export class RateLimitedError extends Error {
  constructor(readonly retryAfterSeconds: number) {
    super("Too many requests");
  }
}

export function normalizePublicBookingError(error: ApiError): string {
  switch (error.status) {
    case 400:
      return "Please check your details and try again.";
    case 409:
      return "That time was just taken — please pick another.";
    case 429:
      return "Too many attempts. Please wait before trying again.";
    default:
      return "Unable to complete the booking. Please try again.";
  }
}
