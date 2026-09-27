import { AxiosError, AxiosHeaders, type InternalAxiosRequestConfig } from "axios";
import { describe, expect, it, vi } from "vitest";
import { createWidgetClient } from "./client";
import { createWidgetApi } from "./widget-api";

function reject(config: InternalAxiosRequestConfig, status: number, headers = {}) {
  return Promise.reject(new AxiosError("refused", "ERR_BAD_REQUEST", config, undefined,
    { status, statusText: "refused", headers: new AxiosHeaders(headers), config, data: { message: "refused" } }));
}

describe("widget transport", () => {
  it("renews once for concurrent expired calls and replays with the fresh token", async () => {
    let token = "old";
    const refreshToken = vi.fn(async () => { token = "new"; return token; });
    const client = createWidgetClient({ apiUrl: "https://example.test", getToken: async () => token, refreshToken });
    client.defaults.adapter = async (config) => {
      expect(config.withCredentials).toBe(false);
      expect(config.headers.get("X-Correlation-Id")).toBeTruthy();
      if (config.headers.get("Authorization") === "Bearer old") return reject(config, 401);
      return { status: 200, statusText: "OK", headers: {}, config, data: "done" };
    };
    expect((await Promise.all([client.get("/one"), client.get("/two")])).map((r) => r.data)).toEqual(["done", "done"]);
    expect(refreshToken).toHaveBeenCalledTimes(1);
  });

  it("stops after the replay's second 401", async () => {
    let token = "old";
    const refreshToken = vi.fn(async () => { token = "new"; return token; });
    const client = createWidgetClient({ apiUrl: "https://example.test", getToken: async () => token, refreshToken });
    const adapter = vi.fn((config: InternalAxiosRequestConfig) => reject(config, 401));
    client.defaults.adapter = adapter;
    await expect(createWidgetApi(client).getOrganization()).rejects.toMatchObject({ detail: { status: 401 } });
    expect(adapter).toHaveBeenCalledTimes(2);
    expect(refreshToken).toHaveBeenCalledTimes(1);
  });

  it("preserves Retry-After on 429 without refreshing or replaying", async () => {
    const refreshToken = vi.fn();
    const client = createWidgetClient({ apiUrl: "https://example.test", getToken: async () => "token", refreshToken });
    const adapter = vi.fn((config: InternalAxiosRequestConfig) => reject(config, 429, { "retry-after": "23" }));
    client.defaults.adapter = adapter;
    await expect(createWidgetApi(client).getOrganization()).rejects.toMatchObject({ detail: { status: 429, retryAfterSeconds: 23 } });
    expect(adapter).toHaveBeenCalledTimes(1);
    expect(refreshToken).not.toHaveBeenCalled();
  });
});
