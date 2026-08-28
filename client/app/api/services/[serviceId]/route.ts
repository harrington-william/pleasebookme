import { NextResponse, type NextRequest } from "next/server";

import { deleteServiceOnPlatform } from "@/features/services/services/service-gateway";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

export async function DELETE(
  _request: NextRequest,
  context: { params: Promise<{ serviceId: string }> }
) {
  const { serviceId } = await context.params;
  const parsed = Number(serviceId);

  if (!serviceId || !Number.isInteger(parsed)) {
    return NextResponse.json(
      { message: "Missing or invalid service identifier.", status: 400 },
      { status: 400 }
    );
  }

  try {
    await withAccessToken((accessToken) =>
      deleteServiceOnPlatform(accessToken, parsed)
    );

    return new NextResponse(null, { status: 204 });
  } catch (error) {
    if (error instanceof SessionExpiredError) {
      return NextResponse.json(
        { message: error.message, status: 401 },
        { status: 401 }
      );
    }

    const normalized = normalizeApiError(error);
    return NextResponse.json(normalized, { status: normalized.status || 502 });
  }
}
