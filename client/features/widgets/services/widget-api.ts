import type {
  WidgetCreatePayload,
  WidgetUpdatePayload,
} from "@/features/widgets/schemas/widget-schema";
import type { Widget, WidgetCredentials } from "@/features/widgets/types/widget";
import { ApiRequestError, normalizeApiError } from "@/lib/api-error";
import { bffClient } from "@/lib/axios";

export async function generateWidgetCredentials(): Promise<WidgetCredentials> {
  try {
    const response = await bffClient.post<WidgetCredentials>(
      "/widgets/credentials"
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

const DUPLICATE_PUBLIC_KEY_MESSAGE =
  "That public key is already in use. Generate a new pair and try again.";

export async function createWidget(
  input: WidgetCreatePayload
): Promise<Widget> {
  try {
    const response = await bffClient.post<Widget>("/widgets", input);
    return response.data;
  } catch (error) {
    const detail = normalizeApiError(error);

    // A 409 on create can only be a public-key collision. The platform's own
    // wording quotes the key, which tells the reader nothing about what to do;
    // the BFF has already normalised the body, so the override goes here rather
    // than through normalizeApiError's statusOverrides.
    throw new ApiRequestError(
      detail.status === 409
        ? { ...detail, message: DUPLICATE_PUBLIC_KEY_MESSAGE }
        : detail
    );
  }
}

export async function updateWidget(
  widgetId: number,
  input: WidgetUpdatePayload
): Promise<Widget> {
  try {
    const response = await bffClient.put<Widget>(
      `/widgets/${widgetId}`,
      input
    );
    return response.data;
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}

export async function deleteWidget(widgetId: number): Promise<void> {
  try {
    await bffClient.delete(`/widgets/${widgetId}`);
  } catch (error) {
    throw new ApiRequestError(normalizeApiError(error));
  }
}
