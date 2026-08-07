import { TriangleAlert } from "lucide-react";

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
