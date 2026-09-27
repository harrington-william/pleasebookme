import axios from "axios";
import { afterEach, describe, expect, it, vi } from "vitest";
import { createSession } from "./session";

vi.mock("axios", () => ({ default: { post: vi.fn(), isAxiosError: () => false } }));
afterEach(() => { vi.clearAllMocks(); vi.unstubAllGlobals(); });

const options = { apiUrl: "https://example.test", publicKey: "public", secretKey: "secret" };

describe("widget memory session", () => {
  it("shares bootstrap and refresh across concurrent requests", async () => {
    vi.stubGlobal("window", { location: { origin: "https://tenant.test" } });
    vi.mocked(axios.post).mockResolvedValueOnce({ data: { accessToken: "first" } });
    const session = createSession(options);
    expect(await Promise.all([session.getToken(), session.getToken()])).toEqual(["first", "first"]);
    expect(axios.post).toHaveBeenCalledTimes(1);
    expect(axios.post).toHaveBeenCalledWith("https://example.test/api/v1/auth/widget/bootstrap",
      { publicKey: "public", secretKey: "secret", origin: "https://tenant.test" }, expect.any(Object));
    vi.mocked(axios.post).mockResolvedValueOnce({ data: { accessToken: "second" } });
    expect(await Promise.all([session.refresh(), session.refresh(), session.getToken()])).toEqual(["second", "second", "second"]);
    expect(axios.post).toHaveBeenCalledTimes(2);
  });

  it("does not retry failed bootstrap and permits a later explicit attempt", async () => {
    vi.stubGlobal("window", { location: { origin: "https://tenant.test" } });
    vi.mocked(axios.post).mockRejectedValueOnce(new Error("offline"));
    const session = createSession(options);
    await expect(session.getToken()).rejects.toMatchObject({ name: "ApiRequestError" });
    expect(axios.post).toHaveBeenCalledTimes(1);
    vi.mocked(axios.post).mockResolvedValueOnce({ data: { accessToken: "recovered" } });
    await expect(session.getToken()).resolves.toBe("recovered");
  });
});
