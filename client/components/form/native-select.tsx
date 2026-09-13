import { ChevronDown } from "lucide-react";
import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

export function NativeSelect({
  className,
  containerClassName,
  disabled,
  ...props
}: ComponentProps<"select"> & { containerClassName?: string }) {
  return (
    <div className={cn("relative", containerClassName)}>
      <select
        disabled={disabled}
        className={cn(
          "h-9 w-full appearance-none rounded-lg border border-border bg-background px-md pr-xl text-body-md text-foreground transition-colors focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50",
          className
        )}
        {...props}
      />
      <span className="pointer-events-none absolute inset-x-0 bottom-0 flex h-9 items-center justify-end pr-sm">
        <ChevronDown
          className={cn("size-4 text-muted-foreground", disabled && "opacity-50")}
          aria-hidden="true"
        />
      </span>
    </div>
  );
}
