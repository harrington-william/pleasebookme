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
