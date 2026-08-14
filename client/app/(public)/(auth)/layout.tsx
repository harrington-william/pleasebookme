import type { ReactNode } from "react";

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <main className="flex flex-grow items-center justify-center p-md md:p-2xl">
      {children}
    </main>
  );
}
