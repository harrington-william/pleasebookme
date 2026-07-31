import { NextResponse } from "next/server";

import { getSessionActor } from "@/lib/session";

/**
 * GET /api/auth/me
 *
 * RESERVED ENDPOINT — reserved on the frontend ahead of backend support.
 *
 * The platform has NO "current user" route today. AuthController exposes only
 * register/login/refresh, and UserController's lookups all require an explicit
 * id/email/phone path variable rather than deriving the user from the
 * authenticated principal.
 *
 * So this returns what can be known without the server's help: the identity
 * claims carried in the access token. That is enough to answer "am I signed
 * in, and as which actor", but it carries no profile data — no username, name,
 * email, roles or organization — because the JWT simply does not contain them.
 *
 * WHEN GET /api/v1/auth/me LANDS: replace the body below with a gateway call
 * that forwards the access token via `bearer()` and returns the real profile.
 * The route's URL and contract are intentionally shaped so that swap requires
 * no changes at any call site.
 *
 * NOT AN AUTHORIZATION CHECK: the claims are decoded, not verified (see
 * lib/jwt.ts). Never gate protected data on this response alone.
 */
export async function GET() {
  const actor = await getSessionActor();

  if (!actor) {
    return NextResponse.json(
      { message: "Not authenticated.", status: 401 },
      { status: 401 }
    );
  }

  return NextResponse.json(actor, { status: 200 });
}
