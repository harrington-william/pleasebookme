"use client";

import { createContext, useMemo, type ReactNode } from "react";
import { createSession } from "./session";
import { createWidgetClient } from "../api/client";
import { createWidgetApi } from "../api/widget-api";
import type { BookingApi } from "../../ecosystems/barbershop/logic/types";

export const PleaseBookMeContext = createContext<{ api: BookingApi } | null>(null);

// The consuming site owns configuration; props keep the copied library host-independent.
export function PleaseBookMeProvider({ apiUrl, publicKey, secretKey, children }: {
  apiUrl: string; publicKey: string; secretKey: string; children: ReactNode;
}) {
  const value = useMemo(() => {
    const session = createSession({ apiUrl, publicKey, secretKey });
    const client = createWidgetClient({ apiUrl, getToken: session.getToken, refreshToken: session.refresh });

    return { api: createWidgetApi(client) };
  }, [apiUrl, publicKey, secretKey]);

  return <PleaseBookMeContext.Provider value={value}>{children}</PleaseBookMeContext.Provider>;
}
