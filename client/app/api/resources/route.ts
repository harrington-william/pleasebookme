import { NextResponse, type NextRequest } from "next/server";

import { createResourceOnPlatform } from "@/features/resources/services/resource-gateway";
import type {
  ResourceRequest,
  ResourceStatus,
} from "@/features/resources/types/resource";
import { normalizeApiError } from "@/lib/api-error";
import {
  SessionExpiredError,
  withAccessToken,
} from "@/lib/authenticated-platform-request";

const RESOURCE_STATUSES = new Set<ResourceStatus>([
  "ACTIVE",
  "INACTIVE",
  "MAINTENANCE",
  "RETIRED",
]);

function isResourceRequest(body: unknown): body is ResourceRequest {
  if (typeof body !== "object" || body === null) return false;

  const request = body as Partial<ResourceRequest>;
  return (
    typeof request.resourceTypeId === "number" &&
    typeof request.name === "string" &&
    typeof request.slug === "string" &&
    typeof request.status === "string" &&
    RESOURCE_STATUSES.has(request.status as ResourceStatus) &&
    (request.description === undefined ||
      typeof request.description === "string") &&
    (request.capacity === undefined || typeof request.capacity === "number")
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

  if (!isResourceRequest(body)) {
    return NextResponse.json(
      { message: "Missing or invalid resource fields.", status: 400 },
      { status: 400 }
    );
  }

  try {
    const result = await withAccessToken((accessToken) =>
      createResourceOnPlatform(accessToken, body)
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
