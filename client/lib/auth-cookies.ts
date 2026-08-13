export const ACCESS_TOKEN_COOKIE = "pbm_access_token";
export const REFRESH_TOKEN_COOKIE = "pbm_refresh_token";

export const ACCESS_TOKEN_MAX_AGE = 15 * 60;
export const REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60;

export const SIGN_IN_PATH = "/login";

/**
 * Marker appended to /login by any server-side guard that has decided the
 * session is unusable.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * THE INVARIANT THIS EXISTS TO PROTECT:
 *
 *   A redirect to /login must always be accompanied by clearing the session
 *   cookies, or it will bounce straight back.
 *
 * proxy.ts sends a signed-in visitor away from /login. So a guard that decides
 * "no session" and redirects to /login, while leaving the refresh cookie in
 * place, hands proxy.ts a cookie that says "signed in" — and the two redirect
 * at each other until the browser gives up with ERR_TOO_MANY_REDIRECTS.
 *
 * A Server Component cannot clear a cookie: cookies().delete() is illegal
 * during render. It can only redirect. So it redirects HERE, and proxy.ts —
 * which can write to the response — does the clearing on its behalf.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * This is a UX marker, never a credential. Anyone can type it, and all it can
 * ever achieve is signing the typist out of their own browser.
 */
export const SESSION_EXPIRED_PARAM = "session";
export const SESSION_EXPIRED_VALUE = "expired";

/** Redirect target for a server-side guard that found an unusable session. */
export const SESSION_EXPIRED_REDIRECT = `${SIGN_IN_PATH}?${SESSION_EXPIRED_PARAM}=${SESSION_EXPIRED_VALUE}`;
