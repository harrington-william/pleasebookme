import type { WeekStart } from "../../ecosystems/barbershop/logic/types";

const WEEKDAY_BY_NAME: Record<string, number> = {
  Mon: 1,
  Tue: 2,
  Wed: 3,
  Thu: 4,
  Fri: 5,
  Sat: 6,
  Sun: 7,
};

function splitDate(dateString: string): [number, number, number] {
  const [year, month, day] = dateString.split("-").map(Number);
  return [year, month, day];
}

function padded(value: number): string {
  return String(value).padStart(2, "0");
}

/** Local YYYY-MM-DD without toISOString() shifting the value into UTC. */
export function toDateString(date: Date): string {
  return `${date.getFullYear()}-${padded(date.getMonth() + 1)}-${padded(date.getDate())}`;
}

export function zonedParts(epochMs: number, zone: string) {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: zone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hourCycle: "h23",
    weekday: "short",
  }).formatToParts(epochMs);
  const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));

  return {
    year: Number(values.year),
    month: Number(values.month),
    day: Number(values.day),
    hour: Number(values.hour) === 24 ? 0 : Number(values.hour),
    minute: Number(values.minute),
    second: Number(values.second),
    weekday: WEEKDAY_BY_NAME[values.weekday],
  };
}

export function localDateInZone(epochMs: number, zone: string): string {
  const parts = zonedParts(epochMs, zone);
  return `${parts.year}-${padded(parts.month)}-${padded(parts.day)}`;
}

export function startOfDayInZone(dateString: string, zone: string): number {
  const [year, month, day] = splitDate(dateString);
  const target = Date.UTC(year, month - 1, day);
  const seen = zonedParts(target, zone);
  const seenAsUtc = Date.UTC(
    seen.year,
    seen.month - 1,
    seen.day,
    seen.hour,
    seen.minute,
    seen.second
  );
  let candidate = target - (seenAsUtc - target);

  // The offset at the candidate can differ around DST, so derive it once more.
  const check = zonedParts(candidate, zone);
  if (
    check.year !== year ||
    check.month !== month ||
    check.day !== day ||
    check.hour !== 0 ||
    check.minute !== 0
  ) {
    const checkAsUtc = Date.UTC(
      check.year,
      check.month - 1,
      check.day,
      check.hour,
      check.minute,
      check.second
    );
    candidate -= checkAsUtc - target;
  }

  return candidate;
}

export function endOfDayInZone(dateString: string, zone: string): number {
  return startOfDayInZone(addDays(dateString, 1), zone);
}

export function todayInZone(zone: string): string {
  return localDateInZone(Date.now(), zone);
}

export function addDays(dateString: string, amount: number): string {
  const [year, month, day] = splitDate(dateString);
  const date = new Date(Date.UTC(year, month - 1, day + amount));
  return `${date.getUTCFullYear()}-${padded(date.getUTCMonth() + 1)}-${padded(date.getUTCDate())}`;
}

export function compareDates(first: string, second: string): number {
  return first === second ? 0 : first < second ? -1 : 1;
}

export function weekdayOf(dateString: string): number {
  const [year, month, day] = splitDate(dateString);
  const weekday = new Date(Date.UTC(year, month - 1, day)).getUTCDay();
  return weekday === 0 ? 7 : weekday;
}

export function monthGrid(
  year: number,
  month: number,
  weekStartsOn: 0 | 1
): { dateString: string; inMonth: boolean }[] {
  const first = `${year}-${padded(month)}-01`;
  const firstWeekday = weekdayOf(first) % 7;
  const offset = (firstWeekday - weekStartsOn + 7) % 7;
  const gridStart = addDays(first, -offset);

  return Array.from({ length: 42 }, (_, index) => {
    const dateString = addDays(gridStart, index);
    return { dateString, inMonth: splitDate(dateString)[1] === month };
  });
}

export function scheduleDatesFor(
  localDate: string,
  visitorZone: string,
  scheduleZone: string
): string[] {
  const first = localDateInZone(
    startOfDayInZone(localDate, visitorZone),
    scheduleZone
  );
  const last = localDateInZone(
    endOfDayInZone(localDate, visitorZone) - 1,
    scheduleZone
  );
  return first === last ? [first] : [first, last];
}

export function formatSlotTime(iso: string, zone: string): string {
  return new Intl.DateTimeFormat(undefined, {
    timeZone: zone,
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(iso));
}

export function formatLongDate(
  dateString: string,
  zone: string,
  form: "short" | "long" = "short"
): string {
  return new Intl.DateTimeFormat(undefined, {
    timeZone: zone,
    weekday: "long",
    month: form === "long" ? "long" : "short",
    day: "numeric",
    year: form === "long" ? "numeric" : undefined,
  }).format(startOfDayInZone(dateString, zone));
}

export function formatShortDate(dateString: string): string {
  const [year, month, day] = splitDate(dateString);
  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
  }).format(new Date(Date.UTC(year, month - 1, day)));
}

export function formatZoneLabel(zone: string): string {
  const parts = new Intl.DateTimeFormat(undefined, {
    timeZone: zone,
    timeZoneName: "shortOffset",
  }).formatToParts(Date.now());
  const offset = parts.find((part) => part.type === "timeZoneName")?.value;
  return offset ? `${zone} (${offset})` : zone;
}

export function weekStartsOnFrom(weekStart: WeekStart): 0 | 1 {
  return weekStart === "SUNDAY" ? 0 : 1;
}
