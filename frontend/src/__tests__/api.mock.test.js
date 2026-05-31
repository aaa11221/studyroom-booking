const { createApiClient } = require("../app");

describe("Mock API 请求测试", () => {
  test("成功请求会返回 data", async () => {
    const fetchMock = jest.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ success: true, data: { name: "Alice" } })
    });
    const api = createApiClient(fetchMock, "");

    const data = await api("/api/auth/me");

    expect(data.data.name).toBe("Alice");
  });

  test("请求会带 credentials 和 Content-Type", async () => {
    const fetchMock = jest.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ success: true, data: [] })
    });
    const api = createApiClient(fetchMock, "");

    await api("/api/user/reservations/available", { method: "GET" });

    expect(fetchMock).toHaveBeenCalledWith("/api/user/reservations/available", {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      method: "GET"
    });
  });

  test("业务失败(success=false)会抛出后端 message", async () => {
    const fetchMock = jest.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ success: false, message: "业务校验失败" })
    });
    const api = createApiClient(fetchMock, "");

    await expect(api("/api/auth/login")).rejects.toThrow("业务校验失败");
  });

  test("HTTP 失败(ok=false)会抛出错误", async () => {
    const fetchMock = jest.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ success: false, message: "server error" })
    });
    const api = createApiClient(fetchMock, "");

    await expect(api("/api/auth/me")).rejects.toThrow("server error");
  });
});
