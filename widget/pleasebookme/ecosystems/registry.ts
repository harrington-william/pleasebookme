import type { ComponentType } from "react";
import { BarbershopBookingWidget } from "./barbershop/variants/kinetic/BarbershopBookingWidget";

export const ECOSYSTEM_WIDGETS: Record<string, ComponentType> = {
  BARBERSHOP: BarbershopBookingWidget,
  GENERAL: BarbershopBookingWidget, // General services are appointment-centic
};

export function widgetForEcosystem(code: string): ComponentType | null {
  return ECOSYSTEM_WIDGETS[code] ?? null;
}
