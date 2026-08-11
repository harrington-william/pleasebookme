import { NextResponse } from "next/server";

import { authorizeGoogleOnboardingOnPlatform } from "@/features/auth/services/auth-gateway";
import { normalizeApiError } from "@/lib/api-error";

/**
 * POST /api/auth/google/authorize
 *
 * BFF boundary for one-shot registration. Returns Google's consent URL for the
 * browser to NAVIGATE to — it does not redirect, because the caller is an XHR
 * and `fetch` would try to load Google's consent page cross-origin.
 *
 * Unauthenticated by nature. No session is read or written here: the account
 * does not exist yet, and none of the three token-bearing helpers apply. The
 * session appears later, at /api/auth/google/handoff.
 *
 * NO `redirectAfter` IS SENT, deliberately. The server already defaults it to
 * "/google/complete" (DefaultGoogleConnectService.DEFAULT_ONBOARDING_REDIRECT_AFTER),
 * and hardcoding the same path on both sides just invites the two to drift.
 * That differs from the connect flow in app/api/integrations/google/connect,
 * which does pass one — there, the client needs a destination the server has no
 * reason to know about. Reach for this field when a real need appears (carrying
 * a `?next=` destination through signup, say), not before.
 */
export async function POST() {
  try {
    const result = await authorizeGoogleOnboardingOnPlatform({});
    return NextResponse.json(result, { status: 200 });
  } catch (error) {
    const normalized = normalizeApiError(error);
    return NextResponse.json(normalized, { status: normalized.status || 502 });
  }
}
