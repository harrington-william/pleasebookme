import type { Metadata } from "next";

import { FeaturesSection } from "@/components/marketing/features-section";
import { HeroSection } from "@/components/marketing/hero-section";
import { MarketingFooter } from "@/components/marketing/marketing-footer";
import { StatsSection } from "@/components/marketing/stats-section";

export const metadata: Metadata = {
  description:
    "Booking infrastructure for modern service businesses. Manage bookings, schedules, customers and resources from one centralized platform.",
};

export default function HomePage() {
  return (
    <>
      <main className="flex-grow">
        <HeroSection />
        <StatsSection />
        <FeaturesSection />
      </main>
      <MarketingFooter />
    </>
  );
}
