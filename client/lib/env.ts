import { z } from "zod";

/* -------------------------------------------------------------------------- */
/* Client environment                                                         */
/* -------------------------------------------------------------------------- */

const clientEnvSchema = z.object({
  appName: z.string().min(1).default("PleaseBookMe"),
  appUrl: z.url().default("http://localhost:3000"),
  googleOAuthEnabled: z
    .string()
    .optional()
    .transform((value) => value === "true" || value === "1"),

  googleClientId: z.string().optional().default(""),
});

function readClientEnv() {
  const parsed = clientEnvSchema.safeParse({
    appName: process.env.NEXT_PUBLIC_APP_NAME,
    appUrl: process.env.NEXT_PUBLIC_APP_URL,
    googleOAuthEnabled: process.env.NEXT_PUBLIC_GOOGLE_OAUTH_ENABLED,
    googleClientId: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
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
/* Server environment                                                         */
/* -------------------------------------------------------------------------- */

const serverEnvSchema = z.object({
  apiBaseUrl: z.url(),
  
  // Upstream request timeout (milliseconds)
  apiTimeoutMs: z.coerce.number().int().positive().default(15_000),
  isProduction: z.boolean(),
});

export type ServerEnv = z.infer<typeof serverEnvSchema>;

let cachedServerEnv: ServerEnv | null = null;

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
