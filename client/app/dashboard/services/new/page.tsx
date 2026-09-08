import type { Metadata } from "next";
import { redirect } from "next/navigation";

import { listMySchedulesOnPlatform } from "@/features/availability/services/availability-gateway";
import type { Schedule } from "@/features/availability/types/availability";
import { ServiceEditor } from "@/features/services/components/service-editor";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";
import { getSessionActor, SESSION_EXPIRED_REDIRECT } from "@/lib/session";

export const metadata: Metadata = {
  title: "Create Service",
  description: "Configure details, pricing, and availability rules.",
};

export default async function CreateServicePage() {
  const actor = await getSessionActor();
  if (!actor) {
    redirect(SESSION_EXPIRED_REDIRECT);
  }

  let schedules: Schedule[] = [];

  try {
    schedules = await withAccessToken(
      (accessToken) => listMySchedulesOnPlatform(accessToken, actor.subject),
      { allowSessionWrite: false }
    );
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      redirect(SESSION_EXPIRED_REDIRECT);
    }
  }

  return <ServiceEditor mode="create" schedules={schedules} />;
}
