import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function QuickCreateButton({ className }: { className?: string }) {
  return (
    <Button
      type="button"
      disabled
      title="Create flows have not been built yet."
      className={cn("h-9 px-sm text-body-md", className)}
    >
      <Plus className="size-4" aria-hidden="true" />
      Quick Create
    </Button>
  );
}
