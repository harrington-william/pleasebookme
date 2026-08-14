import type { Metadata } from "next";

import { RegisterForm } from "@/features/auth/components/register-form";

export const metadata: Metadata = {
  title: "Create Account",
  description:
    "Create a PleaseBookMe account and initialize your booking infrastructure.",
};

/**
 * Route files stay thin: they own metadata and nothing else. All behaviour
 * lives in features/auth so it can be reused (and tested) independently of
 * routing.
 */
export default function RegisterPage() {
  return <RegisterForm />;
}
