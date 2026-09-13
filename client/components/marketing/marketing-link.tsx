import Link from "next/link";

import type { MarketingLinkItem } from "@/components/marketing/marketing-navigation";
import { cn } from "@/lib/utils";

type MarketingLinkProps = {
  item: MarketingLinkItem;
  className?: string;
};

export function MarketingLink({ item, className }: MarketingLinkProps) {
  if (item.status === "reserved" || !item.href) {
    return (
      <span
        aria-disabled="true"
        title={item.reservedReason}
        className={cn("cursor-default text-muted-foreground/50", className)}
      >
        {item.label}
      </span>
    );
  }

  return (
    <Link
      href={item.href}
      className={cn(
        "text-muted-foreground transition-colors hover:text-foreground",
        className
      )}
    >
      {item.label}
    </Link>
  );
}
