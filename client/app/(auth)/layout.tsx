import type { ReactNode } from "react";

/**
 * Shell for the auth route group.
 *
 * Deliberately has no navigation chrome: registration and sign-in are linear,
 * transactional flows, and the surrounding app frame would only offer exits
 * from a task the user came here to finish.
 *
 * The ambient grid backdrop is not rendered here — it is mounted once globally
 * in the root layout and shows through, since this layout paints no background
 * of its own.
 */
export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <main className="flex flex-grow items-center justify-center p-md md:p-2xl">
      {children}
    </main>
  );
}
