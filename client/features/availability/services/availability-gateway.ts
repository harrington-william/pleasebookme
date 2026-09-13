import axios from "axios";

import type {
  Availability,
  AvailabilityRequest,
  AvailabilityRuleset,
  AvailabilityWindow,
  Schedule,
  ScheduleRequest,
} from "@/features/availability/types/availability";
import { bearer, platformClient } from "@/lib/axios";
import { resolveUserId } from "@/lib/platform-user";

const SCHEDULE_BASE = "/api/v1/schedules";
const AVAILABILITY_BASE = "/api/v1/availabilities";

/**
 * Thrown for both "no schedule with this id" and "this schedule belongs to
 * someone else" — mirroring the disconnect-endpoint precedent in SECURITY.md
 * §12 ("returns the same exception for not-yours as for does-not-exist"), so
 * this endpoint can't be used to probe which schedule ids are real.
 */
export class ScheduleNotFoundError extends Error {
  constructor() {
    super("Availability ruleset not found.");
    this.name = "ScheduleNotFoundError";
  }
}

async function getOwnedScheduleOnPlatform(
  accessToken: string,
  userId: number,
  scheduleId: number
): Promise<Schedule> {
  let schedule: Schedule;

  try {
    const response = await platformClient().get<Schedule>(
      `${SCHEDULE_BASE}/${scheduleId}`,
      { headers: bearer(accessToken) }
    );
    schedule = response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new ScheduleNotFoundError();
    }
    throw error;
  }

  if (schedule.userId !== userId) {
    throw new ScheduleNotFoundError();
  }

  return schedule;
}

export async function createScheduleOnPlatform(
  accessToken: string,
  request: ScheduleRequest
): Promise<Schedule> {
  const response = await platformClient().post<Schedule>(
    SCHEDULE_BASE,
    request,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

export async function deleteScheduleOnPlatform(
  accessToken: string,
  scheduleId: number
): Promise<void> {
  await platformClient().delete(`${SCHEDULE_BASE}/${scheduleId}`, {
    headers: bearer(accessToken),
  });
}

export async function updateScheduleOnPlatform(
  accessToken: string,
  scheduleId: number,
  request: ScheduleRequest
): Promise<Schedule> {
  const response = await platformClient().put<Schedule>(
    `${SCHEDULE_BASE}/${scheduleId}`,
    request,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

export async function createAvailabilityOnPlatform(
  accessToken: string,
  request: AvailabilityRequest
): Promise<Availability> {
  const response = await platformClient().post<Availability>(
    AVAILABILITY_BASE,
    request,
    { headers: bearer(accessToken) }
  );
  return response.data;
}

export async function deleteAvailabilityOnPlatform(
  accessToken: string,
  availabilityId: number
): Promise<void> {
  await platformClient().delete(`${AVAILABILITY_BASE}/${availabilityId}`, {
    headers: bearer(accessToken),
  });
}

export async function listMyAvailabilityRulesetsOnPlatform(
  accessToken: string,
  userUid: string
): Promise<AvailabilityRuleset[]> {
  const userId = await resolveUserId(accessToken, userUid);

  const [schedulesResponse, availabilitiesResponse] = await Promise.all([
    platformClient().get<Schedule[]>(SCHEDULE_BASE, {
      headers: bearer(accessToken),
    }),
    platformClient().get<Availability[]>(AVAILABILITY_BASE, {
      headers: bearer(accessToken),
    }),
  ]);

  const mySchedules = schedulesResponse.data.filter(
    (schedule) => schedule.userId === userId
  );
  const myAvailabilities = availabilitiesResponse.data.filter(
    (availability) => availability.userId === userId
  );

  return mySchedules
    .map((schedule) => ({
      schedule,
      availabilities: myAvailabilities
        .filter(
          (availability) => availability.scheduleId === schedule.scheduleId
        )
        .sort((a, b) => a.startTime.localeCompare(b.startTime)),
    }))
    .sort((a, b) => b.schedule.scheduleId - a.schedule.scheduleId);
}

export async function listMySchedulesOnPlatform(
  accessToken: string,
  userUid: string
): Promise<Schedule[]> {
  const userId = await resolveUserId(accessToken, userUid);

  const response = await platformClient().get<Schedule[]>(SCHEDULE_BASE, {
    headers: bearer(accessToken),
  });

  return response.data
    .filter((schedule) => schedule.userId === userId)
    .sort((a, b) => b.scheduleId - a.scheduleId);
}

export async function createAvailabilityRulesetOnPlatform(
  accessToken: string,
  userUid: string,
  input: { title: string; timezone: string; windows: AvailabilityWindow[] }
): Promise<AvailabilityRuleset> {
  const userId = await resolveUserId(accessToken, userUid);

  const schedule = await createScheduleOnPlatform(accessToken, {
    userId,
    title: input.title,
    timezone: input.timezone,
  });

  const availabilities: Availability[] = [];

  for (const window of input.windows) {
    const availability = await createAvailabilityOnPlatform(accessToken, {
      userId,
      scheduleId: schedule.scheduleId,
      days: window.days,
      startTime: window.startTime,
      endTime: window.endTime,
    });
    availabilities.push(availability);
  }

  return { schedule, availabilities };
}

export async function deleteAvailabilityRulesetOnPlatform(
  accessToken: string,
  scheduleId: number
): Promise<void> {
  await deleteScheduleOnPlatform(accessToken, scheduleId);
}

export async function getAvailabilityRulesetOnPlatform(
  accessToken: string,
  userUid: string,
  scheduleId: number
): Promise<AvailabilityRuleset> {
  const userId = await resolveUserId(accessToken, userUid);
  const schedule = await getOwnedScheduleOnPlatform(
    accessToken,
    userId,
    scheduleId
  );

  const availabilitiesResponse = await platformClient().get<Availability[]>(
    AVAILABILITY_BASE,
    { headers: bearer(accessToken) }
  );

  const availabilities = availabilitiesResponse.data
    .filter((availability) => availability.scheduleId === scheduleId)
    .sort((a, b) => a.startTime.localeCompare(b.startTime));

  return { schedule, availabilities };
}

export async function updateAvailabilityRulesetOnPlatform(
  accessToken: string,
  userUid: string,
  scheduleId: number,
  input: { title: string; timezone: string; windows: AvailabilityWindow[] }
): Promise<AvailabilityRuleset> {
  const userId = await resolveUserId(accessToken, userUid);
  await getOwnedScheduleOnPlatform(accessToken, userId, scheduleId);

  const updatedSchedule = await updateScheduleOnPlatform(
    accessToken,
    scheduleId,
    { userId, title: input.title, timezone: input.timezone }
  );

  // Delete-all-then-recreate: the day/time grid can produce a different
  // number of windows than currently exist (e.g. splitting Mon-Fri into two
  // time ranges), so there is no stable 1:1 mapping from old rows to new ones
  // to PUT in place.
  const existingAvailabilitiesResponse = await platformClient().get<
    Availability[]
  >(AVAILABILITY_BASE, { headers: bearer(accessToken) });

  const existingAvailabilities = existingAvailabilitiesResponse.data.filter(
    (availability) => availability.scheduleId === scheduleId
  );

  for (const availability of existingAvailabilities) {
    await deleteAvailabilityOnPlatform(accessToken, availability.availabilityId);
  }

  const availabilities: Availability[] = [];
  for (const window of input.windows) {
    const availability = await createAvailabilityOnPlatform(accessToken, {
      userId,
      scheduleId,
      days: window.days,
      startTime: window.startTime,
      endTime: window.endTime,
    });
    availabilities.push(availability);
  }

  return { schedule: updatedSchedule, availabilities };
}
