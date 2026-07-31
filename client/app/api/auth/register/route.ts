import { NextResponse, type NextRequest } from "next/server";

import { registerRequestSchema } from "@/features/auth/schemas/auth-schema";
import { normalizeAuthError } from "@/features/auth/services/auth-errors";
import { registerOnPlatform } from "@/features/auth/services/auth-gateway";
import { createSession } from "@/lib/session";
import { decodeJwt } from "@/lib/jwt";

/**
 * POST /api/auth/register
 *
 * BFF boundary. Forwards a registration to the platform, then converts the
 * returned JWTs into httpOnly cookies so they never touch browser JavaScript.
 * The response body deliberately contains NO tokens.
 *
 * Registration is a complete authentication event on the platform (it issues a
 * token pair directly, per SECURITY.md), so a successful call leaves the user
 * signed in — there is no separate login step and no email-verification gate.
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

  // Re-validate server-side: the browser's checks are bypassable.
  const parsed = registerRequestSchema.safeParse(payload);
  if (!parsed.success) {
    return NextResponse.json(
      { message: "Some of the details you entered are not valid.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const tokens = await registerOnPlatform(parsed.data);
    await createSession(tokens);

    const claims = decodeJwt(tokens.accessToken);

    return NextResponse.json(
      {
        subject: claims?.sub ?? null,
        actorType: claims?.actor_type ?? null,
        tenantUid: claims?.tenant ?? null,
      },
      { status: 201 }
    );
  } catch (error) {
    const normalized = normalizeAuthError(error);
    return NextResponse.json(normalized, {
      status: normalized.status || 502,
    });
  }
}
