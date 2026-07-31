import { z } from "zod";

/**
 * Validated environment configuration.
 *
 * Two separate surfaces, because Next.js treats them very differently:
 *
 *   clientEnv    Safe to import anywhere (client or server). Only contains
 *                NEXT_PUBLIC_* values, which are inlined into the browser
 *                bundle at build time.
 *
 *   serverEnv()  Server-only. Throws if called from the browser. Reads secrets
 *                and infrastructure config that must never reach the client.
 *
 * NEXT_PUBLIC_* values must be referenced as complete literal property accesses
 * (`process.env.NEXT_PUBLIC_FOO`) — Next.js performs a static find-and-replace
 * at build time, so dynamic lookups like `process.env[key]` silently resolve to
 * undefined in the browser.
 */

/* -------------------------------------------------------------------------- */
/* Client (public) environment                                                 */
/* -------------------------------------------------------------------------- */

const clientEnvSchema = z.object({
  appName: z.string().min(1).default("PleaseBookMe"),
  appUrl: z.url().default("http://localhost:3000"),
  /**
   * Reserved feature flag. The Spring Boot server has the OAuth2 dependencies
   * and application.yaml registration in place, but no redirect/callback flow
   * (security/oauth/google/ is empty, SecurityConfig never calls oauth2Login).
   * Until that exists, the Google buttons render visibly disabled.
   */
  googleOAuthEnabled: z
    .string()
    .optional()
    .transform((value) => value === "true" || value === "1"),
});

function readClientEnv() {
  const parsed = clientEnvSchema.safeParse({
    appName: process.env.NEXT_PUBLIC_APP_NAME,
    appUrl: process.env.NEXT_PUBLIC_APP_URL,
    googleOAuthEnabled: process.env.NEXT_PUBLIC_GOOGLE_OAUTH_ENABLED,
  });

  if (!parsed.success) {
    throw new Error(
      `Invalid public environment configuration:\n${formatIssues(parsed.error)}`
    );
  }

  return parsed.data;
}

export const clientEnv = readClientEnv();

export type ClientEnv = typeof clientEnv;

/* -------------------------------------------------------------------------- */
/* Server (private) environment                                                */
/* -------------------------------------------------------------------------- */

const serverEnvSchema = z.object({
  /** Base origin of the Spring Boot reservation platform. */
  apiBaseUrl: z.url(),
  /** Upstream request timeout, in milliseconds. */
  apiTimeoutMs: z.coerce.number().int().positive().default(15_000),
  isProduction: z.boolean(),
});

export type ServerEnv = z.infer<typeof serverEnvSchema>;

let cachedServerEnv: ServerEnv | null = null;

/**
 * Server-only configuration. Lazily validated so that merely importing this
 * module from a shared file does not blow up a client bundle.
 */
export function serverEnv(): ServerEnv {
  if (typeof window !== "undefined") {
    throw new Error(
      "serverEnv() was called in the browser. Server configuration must never " +
        "reach the client — use clientEnv, or move this call into a Route " +
        "Handler / Server Component."
    );
  }

  if (cachedServerEnv) return cachedServerEnv;

  const parsed = serverEnvSchema.safeParse({
    apiBaseUrl: process.env.API_BASE_URL,
    apiTimeoutMs: process.env.API_TIMEOUT_MS,
    isProduction: process.env.NODE_ENV === "production",
  });

  if (!parsed.success) {
    throw new Error(
      `Invalid server environment configuration:\n${formatIssues(parsed.error)}\n` +
        "Copy .env.example to .env and fill in the required values."
    );
  }

  cachedServerEnv = parsed.data;
  return cachedServerEnv;
}

/* -------------------------------------------------------------------------- */

function formatIssues(error: z.ZodError): string {
  return error.issues
    .map((issue) => `  - ${issue.path.join(".") || "(root)"}: ${issue.message}`)
    .join("\n");
}
