import { NextResponse, type NextRequest } from "next/server";

import { normalizeGoogleSignInError } from "@/features/auth/services/auth-errors";
import { signInWithGoogleOnPlatform } from "@/features/auth/services/auth-gateway";
import { decodeJwt } from "@/lib/jwt";
import { createSession } from "@/lib/session";

/**
 * POST /api/auth/google
 *
 * BFF boundary for Google Sign-In. Takes the ID token that Google Identity
 * Services minted in the browser, hands it to the platform, and converts the
 * returned platform JWTs into httpOnly cookies. No tokens in the response body.
 *
 * The Google ID token is NOT stored anywhere: it is a one-shot assertion of
 * identity that the platform verifies and discards. Only the platform's own
 * access/refresh pair persists, exactly as with password login.
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

  const idToken =
    typeof payload === "object" &&
    payload !== null &&
    typeof (payload as { idToken?: unknown }).idToken === "string"
      ? (payload as { idToken: string }).idToken
      : null;

  if (!idToken) {
    return NextResponse.json(
      { message: "Missing Google credential.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const tokens = await signInWithGoogleOnPlatform({ idToken });
    await createSession(tokens);

    const claims = decodeJwt(tokens.accessToken);

    return NextResponse.json(
      {
        subject: claims?.sub ?? null,
        actorType: claims?.actor_type ?? null,
        tenantUid: claims?.tenant ?? null,
      },
      { status: 200 }
    );
  } catch (error) {
    const normalized = normalizeGoogleSignInError(error);
    return NextResponse.json(normalized, {
      status: normalized.status || 502,
    });
  }
}
