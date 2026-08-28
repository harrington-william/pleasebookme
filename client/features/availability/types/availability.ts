export interface Schedule {
  scheduleId: number;
  userId: number;
  title: string;
  timezone: string;
  createdAt: string;
  updatedAt: string;
}

// POST /api/v1/schedules, PUT /api/v1/schedules/{scheduleId}
export interface ScheduleRequest {
  userId: number;
  title: string;
  timezone?: string;
}

export interface Availability {
  availabilityId: number;
  userId: number;
  scheduleId: number;
  days: number[];
  startTime: string;
  endTime: string;
  createdAt: string;
  updatedAt: string;
}

// POST /api/v1/availabilities, PUT /api/v1/availabilities/{availabilityId}
export interface AvailabilityRequest {
  userId: number;
  scheduleId: number;
  days: number[];
  startTime: string;
  endTime: string;
}

export interface AvailabilityWindow {
  days: number[];
  startTime: string;
  endTime: string;
}

export interface AvailabilityRuleset {
  schedule: Schedule;
  availabilities: Availability[];
}

export interface CreateAvailabilityRulesetInput {
  title: string;
  timezone: string;
  windows: AvailabilityWindow[];
}

export const DAY_DEFINITIONS = [
  { value: 1, label: "Monday", short: "Mon" },
  { value: 2, label: "Tuesday", short: "Tue" },
  { value: 3, label: "Wednesday", short: "Wed" },
  { value: 4, label: "Thursday", short: "Thu" },
  { value: 5, label: "Friday", short: "Fri" },
  { value: 6, label: "Saturday", short: "Sat" },
  { value: 0, label: "Sunday", short: "Sun" },
] as const;

// Week start preference
export const DAY_DISPLAY_ORDER: number[] = DAY_DEFINITIONS.map(
  (day) => day.value
);
