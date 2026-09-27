import type { ComponentProps } from "react";
import { cn } from "../lib/cn";

export function Button({ className, type = "button", ...props }: ComponentProps<"button">) {
  return <button type={type} className={cn("inline-flex h-12 cursor-pointer items-center justify-center gap-xs rounded-lg bg-primary px-lg text-body-md font-medium text-primary-foreground transition-colors hover:bg-primary/80 focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50", className)} {...props} />;
}
