import { NextResponse, type NextRequest } from "next/server";

import {
  ACCESS_TOKEN_COOKIE,
  REFRESH_TOKEN_COOKIE,
  SESSION_EXPIRED_PARAM,
  SESSION_EXPIRED_VALUE,
} from "@/lib/auth-cookies";

// Protected route
const PROTECTED_PREFIXES = ["/dashboard"];

const GUEST_ONLY_ROUTES = ["/login", "/register"];

function clearSession<T extends NextResponse>(response: T): T {
  response.cookies.delete(ACCESS_TOKEN_COOKIE);
  response.cookies.delete(REFRESH_TOKEN_COOKIE);

  return response;
}

export function proxy(request: NextRequest) {
  const { pathname, searchParams } = request.nextUrl;

  const hasSession = Boolean(request.cookies.get(REFRESH_TOKEN_COOKIE)?.value);

  if (GUEST_ONLY_ROUTES.includes(pathname)) {
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
    target.searchParams.set("next", pathname);

    return clearSession(NextResponse.redirect(target));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!api|_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)",
  ],
};
