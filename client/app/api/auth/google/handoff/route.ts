import { NextResponse, type NextRequest } from "next/server";

import { exchangeGoogleHandoffOnPlatform } from "@/features/auth/services/auth-gateway";
import { normalizeApiError } from "@/lib/api-error";
import { decodeJwt } from "@/lib/jwt";
import { createSession } from "@/lib/session";

/**
 * POST /api/auth/google/handoff
 *
 * BFF boundary. Trades the callback's single-use code for a platform token pair
 * and stores it in httpOnly cookies. The response body contains NO tokens.
 *
 * WHY THIS ROUTE EXISTS AT ALL: the Google callback lands on Spring Boot, which
 * cannot write cookies for this origin. Rather than passing tokens through the
 * URL — where they would land in browser history, `Referer`, and every proxy log
 * on the way — the platform hands over an opaque code that is worthless once
 * spent. This is the only place in the one-shot flow that touches cookies, and
 * it is deliberately the same shape as /api/auth/login: same createSession, same
 * claim extraction, same identity-only response.
 */
export async function POST(request: NextRequest) {
  let payload: unknown;

  try {
    payload = await request.json();
  } catch {
    return NextResponse.json(
      { message: "Malformed request body.", status: 400 },
      { status: 400 }
    );
  }

  const code = (payload as { code?: unknown } | null)?.code;

  if (typeof code !== "string" || code.trim().length === 0) {
    return NextResponse.json(
      { message: "Missing sign-in code.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const tokens = await exchangeGoogleHandoffOnPlatform(code);
    await createSession(tokens);

    const claims = decodeJwt(tokens.accessToken);

    return NextResponse.json(
      {
        subject: claims?.sub ?? null,
        actorType: claims?.actor_type ?? null,
        // Null is not an error here — see SECURITY.md on tenant optionality.
        tenantUid: claims?.tenant ?? null,
      },
      { status: 200 }
    );
  } catch (error) {
    const normalized = normalizeApiError(error, {
      statusOverrides: {
        // The platform's InvalidSessionHandoffException message is correct but
        // internal ("unknown, expired, or already used"). Unknown / expired /
        // replayed are indistinguishable by design, and all three mean the same
        // thing to a user: start again.
        401: "That sign-in link has expired. Please start again.",
      },
    });

    return NextResponse.json(normalized, { status: normalized.status || 502 });
  }
}
