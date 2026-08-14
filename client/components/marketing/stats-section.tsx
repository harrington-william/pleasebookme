const STATS = [
  { value: "10k+", label: "Businesses Onboarded" },
  { value: "50M+", label: "Appointments Managed" },
  { value: "99.99%", label: "API Uptime" },
  { value: "< 50ms", label: "Response Time" },
] as const;

export function StatsSection() {
  return (
    <section className="border-y border-border bg-surface/30 py-xl">
      <div className="mx-auto grid max-w-[1440px] grid-cols-2 gap-lg px-lg md:grid-cols-4 lg:px-2xl">
        {STATS.map((stat) => (
          <div
            key={stat.label}
            className="flex flex-col items-center justify-center text-center"
          >
            <span className="mb-base text-headline-lg text-foreground">
              {stat.value}
            </span>
            <span className="text-mono-label font-mono tracking-wider text-muted-foreground uppercase">
              {stat.label}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}
