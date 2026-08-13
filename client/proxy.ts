import { NextResponse, type NextRequest } from "next/server";

import {
  ACCESS_TOKEN_COOKIE,
  REFRESH_TOKEN_COOKIE,
  SESSION_EXPIRED_PARAM,
  SESSION_EXPIRED_VALUE,
} from "@/lib/auth-cookies";

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
 *
 * ⚠ Because this check is optimistic, it WILL sometimes disagree with the
 * server-side guards in the pages. That disagreement is only safe as long as
 * every redirect to /login also clears the session cookies — otherwise the two
 * sides redirect at each other forever. See SESSION_EXPIRED_REDIRECT in
 * lib/auth-cookies.ts for the full statement of that invariant. Both places
 * this file sends a visitor to /login therefore call `clearSession`.
 */

/** Routes that require a session. Prefix-matched. */
const PROTECTED_PREFIXES = ["/dashboard"];

/** Routes an already-signed-in user should not see. Exact-matched. */
const GUEST_ONLY_ROUTES = ["/login", "/register"];

/**
 * Strips the session cookies from an outgoing response.
 *
 * Deleting them on the response — rather than trusting the browser to have
 * already dropped them — is what breaks the redirect cycle: once they are gone
 * the next request looks like a plain signed-out visitor to every layer.
 */
function clearSession<T extends NextResponse>(response: T): T {
  response.cookies.delete(ACCESS_TOKEN_COOKIE);
  response.cookies.delete(REFRESH_TOKEN_COOKIE);
  return response;
}

export function proxy(request: NextRequest) {
  const { pathname, searchParams } = request.nextUrl;

  const hasSession = Boolean(request.cookies.get(REFRESH_TOKEN_COOKIE)?.value);

  if (GUEST_ONLY_ROUTES.includes(pathname)) {
    // A server-side guard concluded the session is unusable and bounced the
    // visitor here. It could not clear the cookies itself (illegal during
    // render), so do it now — and, critically, do NOT send them back to
    // /dashboard on the strength of the very cookie being discarded.
    if (searchParams.get(SESSION_EXPIRED_PARAM) === SESSION_EXPIRED_VALUE) {
      return clearSession(NextResponse.next());
    }

    if (hasSession) {
      return NextResponse.redirect(new URL("/dashboard", request.url));
    }

    return NextResponse.next();
  }

  const isProtected = PROTECTED_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`)
  );

  if (isProtected && !hasSession) {
    const target = new URL("/login", request.url);
    // Preserve the destination so sign-in can return the user to it.
    target.searchParams.set("next", pathname);
    // A leftover access cookie with no refresh cookie is not a session; drop it
    // so no later layer mistakes it for one.
    return clearSession(NextResponse.redirect(target));
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
