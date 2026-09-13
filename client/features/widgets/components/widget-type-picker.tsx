"use client";

import {
  AppWindow,
  CodeXml,
  LayoutTemplate,
  Maximize2,
  type LucideIcon,
} from "lucide-react";
import { useFormContext, useWatch } from "react-hook-form";

import type { WidgetFormValues } from "@/features/widgets/schemas/widget-schema";
import {
  SUPPORTED_WIDGET_TYPES,
  WIDGET_TYPE_LABELS,
  WIDGET_TYPES,
  type WidgetType,
} from "@/features/widgets/types/widget";
import { cn } from "@/lib/utils";

const TYPE_OPTIONS: Record<WidgetType, { icon: LucideIcon; hint: string }> = {
  INLINE: { icon: LayoutTemplate, hint: "Renders inside your page" },
  POPUP: { icon: AppWindow, hint: "Opens over your page" },
  FULL_PAGE: { icon: Maximize2, hint: "A hosted booking page" },
  EMBEDDED: { icon: CodeXml, hint: "Script-injected container" },
};

function isSupported(type: WidgetType): boolean {
  return (SUPPORTED_WIDGET_TYPES as readonly WidgetType[]).includes(type);
}

/**
 * Real radio inputs, visually hidden, so keyboard and assistive tech get radio
 * semantics for free. The platform accepts all four types but this client
 * ships one; the others stay visible and disabled so the roadmap is legible
 * without pretending to work.
 */
export function WidgetTypePicker() {
  const { control, register } = useFormContext<WidgetFormValues>();
  const selected = useWatch({ control, name: "type" });

  return (
    <fieldset className="grid grid-cols-2 gap-sm md:grid-cols-4">
      <legend className="sr-only">Widget type</legend>

      {WIDGET_TYPES.map((type) => {
        const { icon: Icon, hint } = TYPE_OPTIONS[type];
        const supported = isSupported(type);
        const active = selected === type;

        return (
          <label
            key={type}
            title={supported ? undefined : "Not available in this version."}
            className={cn(
              "relative flex flex-col gap-sm rounded-lg border p-md text-left transition-colors",
              active
                ? "border-primary bg-primary/5"
                : "border-border bg-background",
              supported
                ? "cursor-pointer hover:border-surface-hover"
                : "cursor-not-allowed opacity-50",
              "focus-within:ring-2 focus-within:ring-ring"
            )}
          >
            <input
              type="radio"
              value={type}
              disabled={!supported}
              className="sr-only"
              {...register("type")}
            />

            {active ? (
              <span
                aria-hidden="true"
                className="absolute top-sm right-sm size-1.5 rounded-full bg-primary"
              />
            ) : null}

            {!supported ? (
              <span className="absolute top-xs right-xs rounded-sm border border-border bg-muted/40 px-xs text-label-md text-muted-foreground">
                Soon
              </span>
            ) : null}

            <Icon className="size-5 text-muted-foreground" aria-hidden="true" />

            <span className="flex flex-col gap-base">
              <span className="text-body-md text-foreground">
                {WIDGET_TYPE_LABELS[type]}
              </span>
              <span className="text-label-md text-muted-foreground">{hint}</span>
            </span>
          </label>
        );
      })}
    </fieldset>
  );
}
