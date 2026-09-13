import type { Metadata } from "next";
import { notFound, redirect } from "next/navigation";

import { WidgetEditor } from "@/features/widgets/components/widget-editor";
import { toEditWidgetFormValues } from "@/features/widgets/schemas/widget-schema";
import {
  getWidgetOnPlatform,
  WidgetNotFoundError,
} from "@/features/widgets/services/widget-gateway";
import type { Widget } from "@/features/widgets/types/widget";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Edit Widget",
  description: "Update this widget's details, origin and keys.",
};

/**
 * Deliberately does not read searchParams — see the create page. Only the
 * public key reaches this Server Component; the platform never returns the
 * secret, so there is nothing sensitive to keep out of the props.
 */
export default async function EditWidgetPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const widgetId = Number(id);

  if (!id || !Number.isInteger(widgetId)) {
    notFound();
  }

  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let widget: Widget;

  try {
    widget = await withAccessToken(
      (accessToken) => getWidgetOnPlatform(accessToken, widgetId),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }

    if (error instanceof WidgetNotFoundError) {
      notFound();
    }

    throw error;
  }

  // Soft-deleted. The list never links here; only a stale URL does, and a
  // revoked widget cannot be edited (the platform answers 409), so treat it as
  // gone rather than render a form that can only fail.
  if (widget.status === "REVOKED") {
    notFound();
  }

  return (
    <WidgetEditor
      mode="edit"
      widgetId={widgetId}
      publicKey={widget.publicKey}
      defaultValues={toEditWidgetFormValues(widget)}
    />
  );
}
