export function googleCalendarUrl(input: {
  title: string;
  startTime: string;
  endTime: string;
  location: string | null;
}): string {
  const compact = (iso: string) => iso.replace(/[-:]/g, "").replace(/\.\d{3}/, "");
  const params = new URLSearchParams({
    action: "TEMPLATE",
    text: input.title,
    dates: `${compact(input.startTime)}/${compact(input.endTime)}`,
  });
  if (input.location) params.set("location", input.location);
  return `https://calendar.google.com/calendar/render?${params.toString()}`;
}
