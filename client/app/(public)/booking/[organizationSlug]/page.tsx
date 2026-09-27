import type { Metadata } from "next";
import { notFound } from "next/navigation";

import { BookingWidget } from "@/features/public-booking/components/booking-widget";
import { PublicPageNotFoundError } from "@/features/public-booking/services/public-booking-errors";
import { fetchPublicOrganization } from "@/features/public-booking/services/public-booking-gateway";

type PageProps = { params: Promise<{ organizationSlug: string }> };

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const { organizationSlug } = await params;
  try {
    const organization = await fetchPublicOrganization(organizationSlug);
    return { title: `${organization.name} — Book an appointment` };
  } catch {
    return { title: "Booking page unavailable" };
  }
}

export default async function PublicOrganizationPage({ params }: PageProps) {
  const { organizationSlug } = await params;
  let organization;
  try {
    organization = await fetchPublicOrganization(organizationSlug);
  } catch (error) {
    if (error instanceof PublicPageNotFoundError) notFound();
    throw error;
  }
  return <BookingWidget organization={organization} />;
}
