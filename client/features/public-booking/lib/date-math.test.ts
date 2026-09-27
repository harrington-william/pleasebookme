import { describe, expect, it } from "vitest";

import {
  endOfDayInZone,
  formatSlotTime,
  formatZoneLabel,
  localDateInZone,
  monthGrid,
  scheduleDatesFor,
  startOfDayInZone,
  toDateString,
  weekdayOf,
} from "./date-math";

describe("public booking date math", () => {
  it("keeps a locally constructed calendar date", () => {
    expect(toDateString(new Date(2026, 9, 24, 0, 30))).toBe("2026-10-24");
  });

  it("finds the date of an instant in named zones", () => {
    const instant = Date.parse("2026-09-23T23:00:00Z");
    expect(localDateInZone(instant, "Australia/Sydney")).toBe("2026-09-24");
    expect(localDateInZone(instant, "America/New_York")).toBe("2026-09-23");
  });

  it("handles the Sydney spring-forward day", () => {
    expect(new Date(startOfDayInZone("2026-10-04", "Australia/Sydney")).toISOString()).toBe("2026-10-03T14:00:00.000Z");
    expect(new Date(startOfDayInZone("2026-10-05", "Australia/Sydney")).toISOString()).toBe("2026-10-04T13:00:00.000Z");
    expect(endOfDayInZone("2026-10-04", "Australia/Sydney") - startOfDayInZone("2026-10-04", "Australia/Sydney")).toBe(23 * 60 * 60 * 1000);
  });

  it("handles the Sydney fall-back day", () => {
    const start = startOfDayInZone("2026-04-05", "Australia/Sydney");
    expect(new Date(start).toISOString()).toBe("2026-04-04T13:00:00.000Z");
    expect(endOfDayInZone("2026-04-05", "Australia/Sydney") - start).toBe(25 * 60 * 60 * 1000);
  });

  it("handles both New York DST transitions", () => {
    const spring = startOfDayInZone("2026-03-08", "America/New_York");
    const fall = startOfDayInZone("2026-11-01", "America/New_York");
    expect(new Date(spring).toISOString()).toBe("2026-03-08T05:00:00.000Z");
    expect(endOfDayInZone("2026-03-08", "America/New_York") - spring).toBe(23 * 60 * 60 * 1000);
    expect(new Date(fall).toISOString()).toBe("2026-11-01T04:00:00.000Z");
    expect(endOfDayInZone("2026-11-01", "America/New_York") - fall).toBe(25 * 60 * 60 * 1000);
  });

  it("maps a visitor day to at most two schedule dates", () => {
    expect(scheduleDatesFor("2026-09-23", "America/New_York", "Australia/Sydney")).toEqual(["2026-09-23", "2026-09-24"]);
    expect(scheduleDatesFor("2026-09-23", "UTC", "UTC")).toEqual(["2026-09-23"]);
  });

  it("builds fixed six-week grids for both week starts", () => {
    const sunday = monthGrid(2026, 10, 0);
    const monday = monthGrid(2026, 10, 1);
    expect(sunday).toHaveLength(42);
    expect(monday).toHaveLength(42);
    expect(sunday[0].dateString).toBe("2026-09-27");
    expect(monday[0].dateString).toBe("2026-09-28");
  });

  it("returns ISO weekdays", () => {
    expect(weekdayOf("2026-09-20")).toBe(7);
    expect(weekdayOf("2026-09-21")).toBe(1);
  });

  it("formats slots and zone offsets in the requested zone", () => {
    expect(formatSlotTime("2026-09-23T23:00:00Z", "Australia/Sydney")).toContain("9:00");
    expect(formatZoneLabel("Australia/Sydney")).toMatch(/GMT[+-]\d+/);
  });
});
