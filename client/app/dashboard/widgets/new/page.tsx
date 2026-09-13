import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { WidgetEditor } from "@/features/widgets/components/widget-editor";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Create Widget",
  description: "Set up an embeddable booking widget in three steps.",
};

/**
 * Deliberately does not read searchParams. The active step is read client-side
 * by WidgetEditor, so switching steps never round-trips to the server, never
 * re-renders this page, and cannot disturb the in-progress form — including a
 * generated key pair that exists nowhere but in that form's state.
 */
export default async function NewWidgetPage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  return <WidgetEditor mode="create" />;
}
