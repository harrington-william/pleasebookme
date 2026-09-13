"use client";

import { createContext, useContext } from "react";

import type { Schedule } from "@/features/availability/types/availability";

export type ServiceEditorContextValue = {
  mode: "create" | "edit";
  schedules: Schedule[];
  timezones: string[];
  onTimezoneTouched: () => void;
};

/**
 * Lives in its own module so the panels can read it without importing the editor
 * that renders them, which would be a cycle.
 */
export const ServiceEditorContext =
  createContext<ServiceEditorContextValue | null>(null);

export function useServiceEditor(): ServiceEditorContextValue {
  const value = useContext(ServiceEditorContext);

  if (!value) {
    throw new Error(
      "Service editor panels must be rendered inside a ServiceEditor."
    );
  }

  return value;
}
