import type { Metadata } from "next";
import { notFound, redirect } from "next/navigation";

import { ServiceEditor } from "@/features/services/components/service-editor";
import { toEditServiceFormValues } from "@/features/services/schemas/service-schema";
import { loadServiceEditorData } from "@/features/services/services/service-editor-data";
import { ServiceNotFoundError } from "@/features/services/services/service-gateway";
import { SessionExpiredError } from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Edit Service",
  description: "Update the details, pricing and booking rules for this service.",
};

/**
 * Deliberately does not read searchParams. The active tab is read client-side by
 * ServiceEditor, so switching tabs never round-trips to the server, never
 * re-renders this page, and cannot disturb the in-progress form.
 */
export default async function EditServicePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const serviceId = Number(id);

  if (!id || !Number.isInteger(serviceId)) {
    notFound();
  }

  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let data;

  try {
    data = await loadServiceEditorData(actor.subject, serviceId);
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    if (error instanceof ServiceNotFoundError) {
      notFound();
    }

    throw error;
  }

  return (
    <ServiceEditor
      mode="edit"
      serviceId={serviceId}
      schedules={data.schedules}
      defaultValues={toEditServiceFormValues(data.entry)}
    />
  );
}
