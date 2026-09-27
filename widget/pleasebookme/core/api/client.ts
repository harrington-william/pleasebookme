import axios, { type InternalAxiosRequestConfig } from "axios";

type RetriedConfig = InternalAxiosRequestConfig & { _retried?: boolean };

function correlationId(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) return crypto.randomUUID();
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function createWidgetClient({ apiUrl, getToken, refreshToken }: {
  apiUrl: string;
  getToken: () => Promise<string>;
  refreshToken: () => Promise<string>;
}) {
  const client = axios.create({
    baseURL: apiUrl, timeout: 20_000, withCredentials: false,
    headers: { "Content-Type": "application/json", Accept: "application/json" },
  });
  let refreshInFlight: Promise<string> | null = null;
  client.interceptors.request.use(async (config) => {
    if (!config.headers.has("X-Correlation-Id")) config.headers.set("X-Correlation-Id", correlationId());
    config.headers.set("Authorization", `Bearer ${await getToken()}`);
    return config;
  });
  client.interceptors.response.use((response) => response, async (error: unknown) => {
    if (!axios.isAxiosError(error)) throw error;
    const config = error.config as RetriedConfig | undefined;
    if (error.response?.status !== 401 || !config || config._retried) throw error;
    config._retried = true;
    // A late 401 can arrive after another request has already replaced the token.
    // Replay with that token instead of starting another bootstrap for the same expiry.
    const current = await getToken();
    if (config.headers.get("Authorization") === `Bearer ${current}`) {
      if (!refreshInFlight) {
        refreshInFlight = refreshToken().finally(() => { refreshInFlight = null; });
      }
      await refreshInFlight;
    }
    return client.request(config);
  });
  return client;
}
