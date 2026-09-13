import { NextResponse, type NextRequest } from "next/server";

import { createResourceTypeOnPlatform } from "@/features/resources/services/resource-gateway";
import type { ResourceTypeRequest } from "@/features/resources/types/resource";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

function isResourceTypeRequest(body: unknown): body is ResourceTypeRequest {
  if (typeof body !== "object" || body === null) return false;

  const request = body as Partial<ResourceTypeRequest>;
  return (
    typeof request.name === "string" &&
    (request.description === undefined ||
      typeof request.description === "string") &&
    (request.icon === undefined || typeof request.icon === "string")
  );
}

export async function POST(request: NextRequest) {
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json(
      { message: "Invalid request body.", status: 400 },
      { status: 400 }
    );
  }

  if (!isResourceTypeRequest(body)) {
    return NextResponse.json(
      { message: "Missing or invalid resource type fields.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const result = await withAccessToken((accessToken) =>
      createResourceTypeOnPlatform(accessToken, body)
    );
    return NextResponse.json(result, { status: 201 });
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
