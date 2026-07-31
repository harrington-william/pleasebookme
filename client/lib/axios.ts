import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from "axios";

import { serverEnv } from "@/lib/env";

/**
 * The client app talks to the Spring Boot platform through a Backend-for-
 * Frontend (BFF) hop, so there are two distinct axios instances and they must
 * not be confused:
 *
 *   browser ──bffClient──▶ Next.js Route Handler ──platformClient──▶ Spring Boot
 *
 *   bffClient        Runs in the BROWSER. Same-origin, so the httpOnly session
 *                    cookie rides along automatically and no CORS negotiation
 *                    is needed (the Spring server currently configures no CORS
 *                    at all, which is exactly why the browser never calls it).
 *
 *   platformClient() Runs on the SERVER only. Targets the Spring Boot origin
 *                    from API_BASE_URL. Never import this into a Client
 *                    Component — it reads server-only configuration.
 */

const CORRELATION_HEADER = "X-Correlation-Id";

function correlationId(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function attachCorrelationId(config: InternalAxiosRequestConfig) {
  if (!config.headers.has(CORRELATION_HEADER)) {
    config.headers.set(CORRELATION_HEADER, correlationId());
  }
  return config;
}

export const bffClient: AxiosInstance = axios.create({
  baseURL: "/api",
  timeout: 20_000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },

  // Resolve only 2xx
  validateStatus: (status) => status >= 200 && status < 300,
});

bffClient.interceptors.request.use(attachCorrelationId);

let cachedPlatformClient: AxiosInstance | null = null;

export function platformClient(): AxiosInstance {
  if (cachedPlatformClient) return cachedPlatformClient;

  const { apiBaseUrl, apiTimeoutMs } = serverEnv();

  const instance = axios.create({
    baseURL: apiBaseUrl,
    timeout: apiTimeoutMs,
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    validateStatus: (status) => status >= 200 && status < 300,
  });

  instance.interceptors.request.use(attachCorrelationId);

  cachedPlatformClient = instance;
  return cachedPlatformClient;
}

export function bearer(accessToken: string) {
  return { Authorization: `Bearer ${accessToken}` } as const;
}
