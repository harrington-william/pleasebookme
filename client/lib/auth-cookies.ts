export const ACCESS_TOKEN_COOKIE = "pbm_access_token";
export const REFRESH_TOKEN_COOKIE = "pbm_refresh_token";

// 15 minutes
export const ACCESS_TOKEN_MAX_AGE = 15 * 60;

// 30 days
export const REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60;

export const SIGN_IN_PATH = "/login";

export const SESSION_EXPIRED_PARAM = "session";
export const SESSION_EXPIRED_VALUE = "expired";

export const SESSION_EXPIRED_REDIRECT = `${SIGN_IN_PATH}?${SESSION_EXPIRED_PARAM}=${SESSION_EXPIRED_VALUE}`;
