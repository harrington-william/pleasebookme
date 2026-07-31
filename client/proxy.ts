import { NextResponse, type NextRequest } from "next/server";

import { REFRESH_TOKEN_COOKIE } from "@/lib/auth-cookies";

/**
 * Route gating.
 *
 * Renamed from `middleware` in Next.js 16 — this file must be `proxy.ts` at the
 * project root and export a function named `proxy`.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * THIS IS AN OPTIMISTIC CHECK ONLY.
 *
 * It looks for the presence of the refresh-token cookie and nothing more: no
 * signature verification, no expiry check, no call to the platform. A cookie
 * containing arbitrary text passes.
 *
 * That is deliberate and is exactly the pattern the Next.js authentication
 * guide prescribes — proxy runs on every request including prefetches, so it
 * must stay cheap. Real enforcement happens where the data is: the Spring Boot
 * platform verifies the JWT on every API call via JwtAuthenticationFilter, and
 * pages re-check the session server-side.
 *
 * Never treat passing this check as authorization.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Presence of the REFRESH token (30 days) is the session signal, not the access
 * token (15 minutes). Gating on the access token would bounce a user to /login
 * every 15 minutes even though their session is still perfectly renewable.
 */

/** Routes that require a session. Prefix-matched. */
const PROTECTED_PREFIXES = ["/dashboard"];

/** Routes an already-signed-in user should not see. Exact-matched. */
const GUEST_ONLY_ROUTES = ["/login", "/register"];

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const hasSession = Boolean(
    request.cookies.get(REFRESH_TOKEN_COOKIE)?.value
  );

  const isProtected = PROTECTED_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`)
  );

  if (isProtected && !hasSession) {
    const target = new URL("/login", request.url);
    // Preserve the destination so sign-in can return the user to it.
    target.searchParams.set("next", pathname);
    return NextResponse.redirect(target);
  }

  if (hasSession && GUEST_ONLY_ROUTES.includes(pathname)) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  return NextResponse.next();
}

export const config = {
  /**
   * Skip Next.js internals and static assets. Without this, the redirect logic
   * above would also intercept CSS, JS chunks and images.
   *
   * `/api` is excluded because the BFF route handlers do their own auth: the
   * login and register endpoints must stay reachable without a session, and
   * /api/auth/me returns a 401 rather than an HTML redirect.
   */
  matcher: [
    "/((?!api|_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)",
  ],
};
