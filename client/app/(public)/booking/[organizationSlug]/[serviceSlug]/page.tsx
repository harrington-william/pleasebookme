import type { Metadata } from "next";
import { notFound } from "next/navigation";

import { BookingWidget } from "@/features/public-booking/components/booking-widget";
import { PublicPageNotFoundError } from "@/features/public-booking/services/public-booking-errors";
import {
  fetchPublicOrganization,
  fetchPublicService,
} from "@/features/public-booking/services/public-booking-gateway";

type PageProps = {
  params: Promise<{ organizationSlug: string; serviceSlug: string }>;
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const { organizationSlug, serviceSlug } = await params;
  try {
    const service = await fetchPublicService(organizationSlug, serviceSlug);
    return { title: `${service.title} · ${service.organization.name}` };
  } catch {
    return { title: "Booking page unavailable" };
  }
}

// A shared service link opens the same widget with step one already made,
// so "change service" still works without leaving the page.
export default async function PublicServicePage({ params }: PageProps) {
  const { organizationSlug, serviceSlug } = await params;
  let organization;
  let service;
  try {
    [organization, service] = await Promise.all([
      fetchPublicOrganization(organizationSlug),
      fetchPublicService(organizationSlug, serviceSlug),
    ]);
  } catch (error) {
    if (error instanceof PublicPageNotFoundError) notFound();
    throw error;
  }
  return <BookingWidget organization={organization} initialService={service} />;
}
