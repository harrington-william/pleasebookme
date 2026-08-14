import {
  Boxes,
  Braces,
  CalendarDays,
  ChartColumn,
  LayoutTemplate,
  Users,
  type LucideIcon,
} from "lucide-react";

import { cn } from "@/lib/utils";

type FeatureAccent = "primary" | "success" | "warning";

type Feature = {
  title: string;
  description: string;
  icon: LucideIcon;
  accent: FeatureAccent;
};

const FEATURES: readonly Feature[] = [
  {
    title: "Intelligent Scheduling",
    description:
      "Complex routing, buffer times, and multi-timezone support handled natively.",
    icon: CalendarDays,
    accent: "primary",
  },
  {
    title: "Customer Management",
    description:
      "Unified CRM profiles with complete booking history and automated communications.",
    icon: Users,
    accent: "success",
  },
  {
    title: "Resource Management",
    description:
      "Track rooms, equipment, and staff availability simultaneously to prevent double bookings.",
    icon: Boxes,
    accent: "warning",
  },
  {
    title: "Embedded Widget",
    description:
      "Drop-in customizable booking flows that integrate seamlessly with your existing website.",
    icon: LayoutTemplate,
    accent: "primary",
  },
  {
    title: "Advanced Analytics",
    description:
      "Real-time utilization metrics, revenue forecasting, and custom report generation.",
    icon: ChartColumn,
    accent: "success",
  },
  {
    title: "Developer API",
    description:
      "REST APIs, robust webhooks, and comprehensive SDKs for total control.",
    icon: Braces,
    accent: "warning",
  },
];

const ACCENT_STYLES: Record<FeatureAccent, string> = {
  primary: "bg-primary/10 text-primary",
  success: "bg-success/10 text-success",
  warning: "bg-warning/10 text-warning",
};

export function FeaturesSection() {
  return (
    <section
      id="features"
      className="mx-auto max-w-[1440px] scroll-mt-16 px-lg py-2xl lg:px-2xl lg:py-[120px]"
    >
      <div className="mb-xl max-w-[600px]">
        <h2 className="mb-md text-headline-lg text-foreground">
          Everything you need to run your service operations.
        </h2>
        <p className="text-body-lg text-muted-foreground">
          A complete suite of tools built on a unified data graph. No more
          disjointed systems.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-md md:grid-cols-2 lg:grid-cols-3">
        {FEATURES.map((feature) => {
          const Icon = feature.icon;

          return (
            <article
              key={feature.title}
              className="rounded-lg border border-border bg-surface p-lg transition-colors hover:border-surface-hover"
            >
              <div
                className={cn(
                  "mb-md flex size-10 items-center justify-center rounded-lg",
                  ACCENT_STYLES[feature.accent]
                )}
              >
                <Icon className="size-5" aria-hidden="true" />
              </div>
              <h3 className="mb-sm text-headline-md text-foreground">
                {feature.title}
              </h3>
              <p className="text-body-md text-muted-foreground">
                {feature.description}
              </p>
            </article>
          );
        })}
      </div>
    </section>
  );
}
