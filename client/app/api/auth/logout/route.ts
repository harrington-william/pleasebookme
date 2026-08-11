import { NextResponse } from "next/server";

import { destroySession } from "@/lib/session";

/**
 * POST /api/auth/logout
 *
 * Clears the session cookies.
 *
 * RESERVED / INCOMPLETE: this is a purely local sign-out. The platform exposes
 * no token-revocation endpoint under /api/v1/auth, so the refresh token stays
 * valid server-side for its full 30-day life even after the browser forgets
 * it. auth.refresh_tokens has a `revoked_at` column and the permission
 * vocabulary already reserves SESSION.REVOKE, so the intended server-side
 * revocation call belongs here once that endpoint exists.
 */
export async function POST() {
  await destroySession();
  return new NextResponse(null, { status: 204 });
}
