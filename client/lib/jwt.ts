import type { PlatformJwtClaims } from "@/features/auth/types/auth";

function decodeBase64Url(segment: string): string {
  const normalized = segment.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(
    normalized.length + ((4 - (normalized.length % 4)) % 4),
    "="
  );

  const binary = atob(padded);

  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

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

export function isJwtExpired(token: string, leewaySeconds = 10): boolean {
  const claims = decodeJwt(token);
  if (!claims || typeof claims.exp !== "number") return true;

  const nowSeconds = Math.floor(Date.now() / 1000);
  return claims.exp - leewaySeconds <= nowSeconds;
}
