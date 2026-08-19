import type { ReactNode } from "react";

import { MarketingHeader } from "@/components/marketing/marketing-header";

export default function PublicLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <MarketingHeader />
      {children}
    </>
  );
}
