import { bearer, platformClient } from "@/lib/axios";

/**
 * TEMPORARY WORKAROUND — see AGENTS.md "Platform contract & known gaps".
 *
 * The JWT `sub` claim (== SessionActor.subject) is the user's UUID
 * (auth.users.uid) — see UserPrincipal.subject / UserPrincipalMapper on the
 * platform — never the numeric auth.users.id that some request DTOs and
 * client-side list filters still need. There is no GET /api/v1/users/uid/{uid}
 * lookup exposed (UserRepository has findByUserUid, but UserController never
 * maps it), so the remaining fallback is to list every user and find the match.
 *
 * Same shape for profileId/organizationId: ProfileResponse already flattens
 * both onto one row, so one GET /api/v1/profiles list plus a filter resolves
 * all three ids from a single extra round trip.
 *
 * This must only be called server-side (inside a Route Handler, via
 * withAccessToken) — the full user/profile lists never reach the browser,
 * only the resolved ids do. Replace the remaining callers with server-derived
 * ownership or /me-backed lookup once each flow has a scoped backend contract.
 */

interface PlatformUserSummary {
  userId: number;
  userUid: string;
}

interface PlatformProfileSummary {
  profileId: number;
  userId: number;
  organizationId: number;
}

export interface CurrentPlatformUser {
  userId: number;
  profileId: number;
  organizationId: number;
}

export async function resolveUserId(
  accessToken: string,
  userUid: string
): Promise<number> {
  const response = await platformClient().get<PlatformUserSummary[]>(
    "/api/v1/users",
    { headers: bearer(accessToken) }
  );

  const match = response.data.find(
    (user) => user.userUid?.toLowerCase() === userUid.toLowerCase()
  );

  if (!match) {
    throw new Error(
      "Could not resolve the signed-in user against the platform's user list."
    );
  }

  return match.userId;
}

/**
 * Resolves userId, profileId and organizationId in one extra round trip
 * (GET /users + GET /profiles), for callers that still need all three — e.g.
 * service catalog list filtering while the backend list endpoint is unscoped.
 */
export async function resolveCurrentPlatformUser(
  accessToken: string,
  userUid: string
): Promise<CurrentPlatformUser> {
  const [userId, profilesResponse] = await Promise.all([
    resolveUserId(accessToken, userUid),
    platformClient().get<PlatformProfileSummary[]>("/api/v1/profiles", {
      headers: bearer(accessToken),
    }),
  ]);

  const profile = profilesResponse.data.find(
    (candidate) => candidate.userId === userId
  );

  if (!profile) {
    throw new Error(
      "Could not resolve the signed-in user's organization profile."
    );
  }

  return {
    userId,
    profileId: profile.profileId,
    organizationId: profile.organizationId,
  };
}
