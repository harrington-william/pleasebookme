import { NextResponse, type NextRequest } from "next/server";

import { registerRequestSchema } from "@/features/auth/schemas/auth-schema";
import { normalizeAuthError } from "@/features/auth/services/auth-errors";
import { registerOnPlatform } from "@/features/auth/services/auth-gateway";
import { createSession } from "@/lib/session";
import { decodeJwt } from "@/lib/jwt";

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

  const parsed = registerRequestSchema.safeParse(payload);
  if (!parsed.success) {
    return NextResponse.json(
      { message: "Payload contains invalid data.", status: 400 },
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
