import type { DashboardSummary } from "@/features/dashboard/types/dashboard";
import { bearer, platformClient } from "@/lib/axios";

const DASHBOARD_SUMMARY_PATH = "/api/v1/dashboard/summary";

export async function getDashboardSummaryOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<DashboardSummary> {
  const response = await platformClient().get<DashboardSummary>(
    DASHBOARD_SUMMARY_PATH,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );

  return response.data;
}
