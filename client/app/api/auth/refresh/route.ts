import { NextResponse } from "next/server";

import { normalizeAuthError } from "@/features/auth/services/auth-errors";
import { refreshOnPlatform } from "@/features/auth/services/auth-gateway";
import {
  createSession,
  destroySession,
  getRefreshToken,
} from "@/lib/session";

/**
 * POST /api/auth/refresh
 *
 * Exchanges the stored refresh token for a fresh pair and rewrites both
 * cookies. The refresh token is read from the httpOnly cookie, never from the
 * request body — the browser cannot see it and must not be able to supply it.
 *
 * The platform ROTATES the pair, so both tokens are re-persisted.
 *
 * RESERVED: nothing calls this automatically yet. Once protected pages exist,
 * a 401 from a data call should trigger this and retry once. Deliberately not
 * wired into an axios interceptor now — an auto-refresh loop with no protected
 * routes to exercise it would be untestable, speculative machinery.
 */
export async function POST() {
  const refreshToken = await getRefreshToken();

  if (!refreshToken) {
    return NextResponse.json(
      { message: "No active session.", status: 401 },
      { status: 401 }
    );
  }

  try {
    const tokens = await refreshOnPlatform(refreshToken);
    await createSession(tokens);
    return new NextResponse(null, { status: 204 });
  } catch (error) {
    const normalized = normalizeAuthError(error);

    // The platform maps expired/revoked refresh tokens to 403. Either way the
    // session is unrecoverable, so clear it rather than leaving the browser
    // holding a token that can never succeed again.
    if (normalized.status === 401 || normalized.status === 403) {
      await destroySession();
    }

    return NextResponse.json(normalized, {
      status: normalized.status || 502,
    });
  }
}
