"use client";

import { useContext } from "react";
import { PleaseBookMeContext } from "./PleaseBookMeProvider";

export function usePleaseBookMe() {
  const context = useContext(PleaseBookMeContext);
  if (!context) throw new Error("Wrap your booking widget in PleaseBookMeProvider before using usePleaseBookMe().");
  return context;
}
