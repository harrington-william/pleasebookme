/**
 * Session cookie names and lifetimes.
 *
 * Deliberately dependency-free — no `next/headers`, no env access, no imports
 * at all. Both lib/session.ts (which runs in Route Handlers) and proxy.ts
 * (which runs ahead of rendering, in its own context) need these constants,
 * and the Next.js proxy documentation warns against proxy pulling in shared
 * modules. Keeping this file inert makes it safe for both.
 *
 * Lifetimes mirror the platform's own token TTLs, from the server's
 * application.yaml:
 *   security.jwt.access-token-life-time:  PT15M
 *   security.jwt.refresh-token-life-time: P30D
 * Keep them in sync if the server changes.
 */

export const ACCESS_TOKEN_COOKIE = "pbm_access_token";
export const REFRESH_TOKEN_COOKIE = "pbm_refresh_token";

export const ACCESS_TOKEN_MAX_AGE = 15 * 60;
export const REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60;
