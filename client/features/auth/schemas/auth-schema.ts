import { z } from "zod";

import type { RegisterRequest } from "@/features/auth/types/auth";

/**
 * Form schemas for the auth screens.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * TEMPORARY: THIS IS CURRENTLY THE ONLY VALIDATION IN THE SYSTEM.
 *
 * The platform's RegisterRequest/LoginRequest DTOs carry `@NotBlank` and
 * nothing else — no @Size, no @Email, no password policy. So every rule below
 * (email format, password strength, length caps) is enforced *only* here, in
 * the browser, and is trivially bypassed by posting straight to the platform.
 *
 * These are UX guardrails, NOT a security control. When the backend grows a
 * real validation layer, mirror it here and delete this notice — the server is
 * the authority and the client should follow it, not the other way round.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Length caps below mirror the actual auth.users column widths so the client
 * never sends something the database would reject:
 *   username VARCHAR(100), name VARCHAR(255), email VARCHAR(255)
 */

const USERNAME_MAX = 100;
const NAME_MAX = 255;
const EMAIL_MAX = 255;

const username = z
  .string()
  .trim()
  .min(3, "Username must be at least 3 characters.")
  .max(USERNAME_MAX, `Username must be at most ${USERNAME_MAX} characters.`)
  .regex(
    /^[a-zA-Z0-9._-]+$/,
    "Use letters, numbers, dots, underscores or hyphens only."
  );

/**
 * Password rules are surfaced individually rather than as one combined
 * message, so the register form can list every unmet requirement at once
 * (react-hook-form `criteriaMode: "all"`).
 */
const password = z
  .string()
  .min(8, "Be at least 8 characters long.")
  .regex(/[a-zA-Z]/, "Contain at least one letter.")
  .regex(/[0-9]/, "Contain at least one number.")
  .regex(/[^a-zA-Z0-9]/, "Contain at least one special character.");

export const registerFormSchema = z
  .object({
    // Split for UX only. The platform stores a single `name` column, so these
    // are joined before the request leaves the browser (see toRegisterRequest).
    firstName: z.string().trim().min(1, "First name is required."),
    lastName: z.string().trim().min(1, "Last name is required."),

    // Not present in the design mock, but RegisterRequest requires it and the
    // platform authenticates by username — so the form must collect it.
    username,

    email: z
      .email("Enter a valid email address.")
      .max(EMAIL_MAX, `Email must be at most ${EMAIL_MAX} characters.`),

    password,

    // Client-only. Never sent to the platform.
    confirmPassword: z.string().min(1, "Confirm your password."),

    // Client-only. The platform records no consent flag today.
    //
    // Modelled as a refined boolean rather than z.literal(true) so the field's
    // TYPE stays `boolean`. With a literal, the inferred type is `true`, which
    // makes an unchecked default (`terms: false`) fail to typecheck — the
    // checkbox has to be able to start unticked.
    terms: z.boolean().refine((accepted) => accepted, {
      error: "You must accept the Terms of Service and Privacy Policy.",
    }),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ["confirmPassword"],
    error: "Passwords do not match.",
  })
  .refine(
    (values) =>
      `${values.firstName} ${values.lastName}`.trim().length <= NAME_MAX,
    {
      path: ["lastName"],
      error: `First and last name combined must be at most ${NAME_MAX} characters.`,
    }
  );

export type RegisterFormValues = z.infer<typeof registerFormSchema>;

export const loginFormSchema = z.object({
  // Labelled "Username" in the UI, not "Email Address" as the mock shows:
  // LoginRequest authenticates by username and the platform exposes no
  // email-based login path.
  username: z.string().trim().min(1, "Username is required."),
  password: z.string().min(1, "Password is required."),
});

export type LoginFormValues = z.infer<typeof loginFormSchema>;

/* -------------------------------------------------------------------------- */
/* Wire schemas — used by the BFF route handlers                              */
/* -------------------------------------------------------------------------- */

/**
 * Shapes accepted by our own /api/auth/* route handlers.
 *
 * The browser's form validation is trivially bypassable, so the BFF re-checks
 * the payload shape before forwarding it to the platform. These validate the
 * *contract* (required fields, column widths) rather than re-running the UX
 * password policy, which is a browser-side concern.
 */
export const registerRequestSchema = z.object({
  username: z.string().trim().min(1).max(USERNAME_MAX),
  password: z.string().min(1),
  email: z.string().trim().min(1).max(EMAIL_MAX),
  name: z.string().trim().min(1).max(NAME_MAX),
  phone: z.string().trim().max(50).optional(),
  locale: z.enum(["en", "vi"]).optional(),
  timezone: z.string().trim().max(100).optional(),
});

export const loginRequestSchema = z.object({
  username: z.string().trim().min(1),
  password: z.string().min(1),
});

/**
 * Maps form values onto the platform's RegisterRequest.
 *
 * Drops the client-only fields (confirmPassword, terms) and joins the two name
 * inputs into the single `name` column the server expects. `phone`, `locale`
 * and `timezone` are omitted entirely rather than sent as null — they are
 * optional server-side and `timezone` has a server default.
 */
export function toRegisterRequest(
  values: RegisterFormValues
): RegisterRequest {
  return {
    username: values.username,
    password: values.password,
    email: values.email,
    name: `${values.firstName} ${values.lastName}`.trim(),
  };
}
