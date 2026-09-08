import { listMySchedulesOnPlatform } from "@/features/availability/services/availability-gateway";
import type { Schedule } from "@/features/availability/types/availability";
import { getServiceCatalogEntryOnPlatform } from "@/features/services/services/service-gateway";
import type { ServiceCatalogEntry } from "@/features/services/types/service";
import { withAccessToken } from "@/lib/authenticated-platform-request";

export interface ServiceEditorData {
  entry: ServiceCatalogEntry;
  schedules: Schedule[];
}

/**
 * Both editor subpages render the same form, and PUT is full-replace — the policies
 * page still has to resend the basics, so both need the whole service plus the
 * schedule list. Loading it in one place keeps the two pages from drifting.
 */
export async function loadServiceEditorData(
  userUid: string,
  serviceId: number
): Promise<ServiceEditorData> {
  return withAccessToken(
    async (accessToken) => {
      const [entry, schedules] = await Promise.all([
        getServiceCatalogEntryOnPlatform(accessToken, serviceId),
        listMySchedulesOnPlatform(accessToken, userUid),
      ]);

      return { entry, schedules };
    },
    { allowSessionWrite: false }
  );
}
