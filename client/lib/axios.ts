import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from "axios";

import { serverEnv } from "@/lib/env";

// bffClient: Runs in the BROWSER, Same-origin
// platformClient(): Server component, targets the Spring Boot origin
// Never use platformClient() in client components

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
