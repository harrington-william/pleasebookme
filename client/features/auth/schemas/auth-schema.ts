import { z } from "zod";

import type { RegisterRequest } from "@/features/auth/types/auth";

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

const password = z
  .string()
  .min(8, "Be at least 8 characters long.")
  .regex(/[a-zA-Z]/, "Contain at least one letter.")
  .regex(/[0-9]/, "Contain at least one number.")
  .regex(/[^a-zA-Z0-9]/, "Contain at least one special character.");

export const registerFormSchema = z
  .object({
    firstName: z.string().trim().min(1, "First name is required."),
    lastName: z.string().trim().min(1, "Last name is required."),

    username,

    email: z
      .email("Enter a valid email address.")
      .max(EMAIL_MAX, `Email must be at most ${EMAIL_MAX} characters.`),

    password,

    // Client only
    confirmPassword: z.string().min(1, "Confirm your password."),

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
  username: z.string().trim().min(1, "Username is required."),
  password: z.string().min(1, "Password is required."),
});

export type LoginFormValues = z.infer<typeof loginFormSchema>;

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
