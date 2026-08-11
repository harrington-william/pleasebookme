import type { Metadata } from "next";

import { LoginForm } from "@/features/auth/components/login-form";

export const metadata: Metadata = {
  title: "Sign In",
  description: "Sign in to your PleaseBookMe workspace.",
};

export default function LoginPage() {
  return <LoginForm />;
}
