import { NextResponse, type NextRequest } from "next/server";

import { loginRequestSchema } from "@/features/auth/schemas/auth-schema";
import { normalizeAuthError } from "@/features/auth/services/auth-errors";
import { loginOnPlatform } from "@/features/auth/services/auth-gateway";
import { createSession } from "@/lib/session";
import { decodeJwt } from "@/lib/jwt";

/**
 * POST /api/auth/login
 *
 * BFF boundary. Exchanges username + password for a platform token pair and
 * stores it in httpOnly cookies. The response body contains NO tokens.
 *
 * Note the platform authenticates by USERNAME, not email.
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

  const parsed = loginRequestSchema.safeParse(payload);
  if (!parsed.success) {
    return NextResponse.json(
      { message: "Enter your username and password.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const tokens = await loginOnPlatform(parsed.data);
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
    const normalized = normalizeAuthError(error);
    return NextResponse.json(normalized, {
      status: normalized.status || 502,
    });
  }
}
