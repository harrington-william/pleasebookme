import { TriangleAlert } from "lucide-react";

/**
 * Form-level failure message (bad credentials, duplicate account, platform
 * unreachable) — as opposed to a field-level validation error.
 *
 * `role="alert"` so assistive tech announces it when it appears after submit.
 * Styled per DESIGN.md "Chips/Badges": a 10% tint of the semantic colour with
 * the solid colour for text, keeping the surface quiet rather than alarming.
 */
export function AuthFormAlert({ message }: { message: string }) {
  return (
    <div
      role="alert"
      className="flex items-start gap-sm rounded-lg border border-destructive/30 bg-destructive/10 px-md py-sm text-body-md text-destructive"
    >
      <TriangleAlert
        className="mt-[2px] size-4 shrink-0"
        aria-hidden="true"
      />
      <span>{message}</span>
    </div>
  );
}
