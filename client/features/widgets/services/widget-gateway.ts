import axios from "axios";

import type {
  WidgetCreatePayload,
  WidgetUpdatePayload,
} from "@/features/widgets/schemas/widget-schema";
import type {
  Widget,
  WidgetCredentials,
  WidgetListResult,
  WidgetPage,
  WidgetQuery,
  WidgetStats,
} from "@/features/widgets/types/widget";
import { bearer, platformClient } from "@/lib/axios";
import { resolveCurrentPlatformUser } from "@/lib/platform-user";

const WIDGET_BASE = "/api/v1/widgets";

export class WidgetNotFoundError extends Error {
  constructor() {
    super("Widget not found.");
    this.name = "WidgetNotFoundError";
  }
}

/**
 * The only platform call whose response carries a plaintext secret. It is
 * generated on demand and persisted nowhere until the widget is saved, so the
 * body must pass through untouched and unlogged.
 */
export async function generateWidgetCredentialsOnPlatform(
  accessToken: string
): Promise<WidgetCredentials> {
  const response = await platformClient().post<WidgetCredentials>(
    `${WIDGET_BASE}/credentials`,
    null,
    { headers: bearer(accessToken) }
  );

  return response.data;
}

export async function createWidgetOnPlatform(
  accessToken: string,
  payload: WidgetCreatePayload
): Promise<Widget> {
  const response = await platformClient().post<Widget>(WIDGET_BASE, payload, {
    headers: bearer(accessToken),
  });

  return response.data;
}

export async function updateWidgetOnPlatform(
  accessToken: string,
  widgetId: number,
  payload: WidgetUpdatePayload
): Promise<Widget> {
  try {
    const response = await platformClient().put<Widget>(
      `${WIDGET_BASE}/${widgetId}`,
      payload,
      { headers: bearer(accessToken) }
    );

    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new WidgetNotFoundError();
    }
    throw error;
  }
}

export async function deleteWidgetOnPlatform(
  accessToken: string,
  widgetId: number
): Promise<void> {
  await platformClient().delete(`${WIDGET_BASE}/${widgetId}`, {
    headers: bearer(accessToken),
  });
}

export async function getWidgetOnPlatform(
  accessToken: string,
  widgetId: number
): Promise<Widget> {
  try {
    const response = await platformClient().get<Widget>(
      `${WIDGET_BASE}/${widgetId}`,
      { headers: bearer(accessToken) }
    );

    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new WidgetNotFoundError();
    }
    throw error;
  }
}

async function getWidgetPageOnPlatform(
  accessToken: string,
  organizationId: number,
  query: WidgetQuery
): Promise<WidgetPage> {
  const response = await platformClient().get<WidgetPage>(WIDGET_BASE, {
    headers: bearer(accessToken),
    params: {
      organizationId,
      page: query.page,
      size: query.size,
      type: query.type,
      status: query.status,
      sort: query.sort,
    },
  });

  return response.data;
}

async function getWidgetStatsOnPlatform(
  accessToken: string,
  organizationId: number
): Promise<WidgetStats> {
  const response = await platformClient().get<WidgetStats>(
    `${WIDGET_BASE}/stats`,
    {
      headers: bearer(accessToken),
      params: { organizationId },
    }
  );

  return response.data;
}

export async function listMyWidgetsOnPlatform(
  accessToken: string,
  userUid: string,
  query: WidgetQuery
): Promise<WidgetListResult> {
  const { organizationId } = await resolveCurrentPlatformUser(
    accessToken,
    userUid
  );

  const [page, stats] = await Promise.all([
    getWidgetPageOnPlatform(accessToken, organizationId, query),
    getWidgetStatsOnPlatform(accessToken, organizationId),
  ]);

  return { page, stats };
}
