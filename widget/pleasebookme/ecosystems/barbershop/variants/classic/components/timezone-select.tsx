"use client";

import { Globe } from "lucide-react";

import { NativeSelect } from "../../../../../core/ui/native-select";
import { formatZoneLabel } from "../../../../../core/lib/date-math";

const timeZones =
  typeof Intl.supportedValuesOf === "function"
    ? Intl.supportedValuesOf("timeZone")
    : ["UTC", "Australia/Sydney", "America/New_York", "Europe/London"];

export function TimezoneSelect({
  value,
  onChange,
}: {
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="flex items-center justify-center gap-xs border-t border-border pt-md">
      <Globe className="size-4 text-muted-foreground" aria-hidden="true" />
      <NativeSelect
        aria-label="Display timezone"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        containerClassName="max-w-[320px]"
        className="cursor-pointer border-0 bg-transparent text-label-md text-muted-foreground focus-visible:ring-1"
      >
        {timeZones.map((zone) => (
          <option key={zone} value={zone}>
            {formatZoneLabel(zone)}
          </option>
        ))}
      </NativeSelect>
    </div>
  );
}
