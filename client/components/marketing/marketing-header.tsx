import Link from "next/link";

import { MarketingLink } from "@/components/marketing/marketing-link";
import { MARKETING_NAV_ITEMS } from "@/components/marketing/marketing-navigation";
import { clientEnv } from "@/lib/env";

export function MarketingHeader() {
  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/80 backdrop-blur-md">
      {/* Logo */}
      <div className="mx-auto flex h-16 max-w-[1440px] items-center justify-between px-lg lg:px-2xl">
        <Link
          href="/"
          className="text-headline-md font-bold tracking-tight text-foreground"
        >
          {clientEnv.appName}
        </Link>

        {/* Nav links */}
        <nav
          aria-label="Marketing sections"
          className="hidden items-center gap-lg md:flex"
        >
          {MARKETING_NAV_ITEMS.map((item) => (
            <MarketingLink
              key={item.label}
              item={item}
              className="text-label-md"
            />
          ))}
        </nav>

        {/* Sign in / register */}
        <div className="flex items-center gap-md">
          <Link
            href="/login"
            className="hidden text-label-md text-foreground transition-colors hover:text-primary md:inline-flex"
          >
            Sign In
          </Link>

          <Link
            href="/register"
            className="inline-flex items-center justify-center rounded-lg bg-primary px-md py-xs text-label-md text-primary-foreground transition-colors hover:bg-primary/80"
          >
            Start for Free
          </Link>
        </div>
      </div>
    </header>
  );
}
