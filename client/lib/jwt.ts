import type { PlatformJwtClaims } from "@/features/auth/types/auth";

/**
 * Unverified JWT payload decoding.
 *
 * SECURITY BOUNDARY — read this before using any of these helpers.
 *
 * Nothing here validates a signature. The Spring Boot platform is the only
 * component that can verify a token (it holds JWT_SECRET), and it re-verifies
 * on every single request through JwtAuthenticationFilter.
 *
 * These helpers exist solely for *optimistic* client-side decisions, in the
 * sense the Next.js docs use the term: deciding which route to render or
 * whether to bother issuing a request. They must never be the only thing
 * standing between a caller and protected data. A forged token trivially
 * passes `decodeJwt`; it will not pass the platform.
 */

/** Decodes base64url without assuming Node's Buffer or the browser's atob. */
function decodeBase64Url(segment: string): string {
  const normalized = segment.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(
    normalized.length + ((4 - (normalized.length % 4)) % 4),
    "="
  );

  const binary = atob(padded);

  // Decode as UTF-8 rather than trusting the binary string: claims can hold
  // non-ASCII characters and atob alone would mangle them.
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

/**
 * Decodes a JWT's payload WITHOUT verifying its signature.
 * Returns null if the token is malformed.
 */
export function decodeJwt(token: string): PlatformJwtClaims | null {
  const segments = token.split(".");
  if (segments.length !== 3) return null;

  try {
    const payload = JSON.parse(decodeBase64Url(segments[1]));
    if (typeof payload !== "object" || payload === null) return null;
    return payload as PlatformJwtClaims;
  } catch {
    return null;
  }
}

/**
 * True when the token is absent, malformed, or past its `exp`.
 *
 * `leewaySeconds` treats a token that is about to expire as already expired,
 * so a request is not fired off milliseconds before the platform would reject
 * it. Defaults to 10s.
 */
export function isJwtExpired(token: string, leewaySeconds = 10): boolean {
  const claims = decodeJwt(token);
  if (!claims || typeof claims.exp !== "number") return true;

  const nowSeconds = Math.floor(Date.now() / 1000);
  return claims.exp - leewaySeconds <= nowSeconds;
}
