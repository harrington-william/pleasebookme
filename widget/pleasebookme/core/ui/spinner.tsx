export function Spinner({ label = "Loading…" }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-sm py-xl text-body-md text-muted-foreground">
      <span className="size-5 rounded-full border-2 border-border border-t-primary" />
      {label}
    </div>
  );
}
