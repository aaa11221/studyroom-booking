const { fireEvent, screen } = require("@testing-library/dom");
const { bindApp } = require("../app");

function createPage() {
  document.body.innerHTML = `
    <main>
      <form id="loginForm">
        <input id="username" />
        <input id="password" />
        <button type="submit">login</button>
      </form>
      <button id="meBtn">me</button>
      <pre id="meResult"></pre>
      <button id="availableBtn">available</button>
      <pre id="availableResult"></pre>
      <button id="logoutBtn">logout</button>
      <pre id="logoutResult"></pre>
    </main>
  `;
}

function delayedResolve(data) {
  return new Promise((resolve) => setTimeout(() => resolve(data), 0));
}

describe("component interaction", () => {
  beforeEach(() => {
    createPage();
  });

  test("login submits API request", async () => {
    const apiClient = jest.fn().mockResolvedValue({
      success: true,
      message: "ok",
      data: { user: { id: "32001041" } }
    });
    bindApp(document, apiClient);

    document.getElementById("username").value = "student_01";
    document.getElementById("password").value = "123456";
    fireEvent.submit(document.getElementById("loginForm"));

    await Promise.resolve();

    expect(apiClient).toHaveBeenCalledWith("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username: "student_01", password: "123456" })
    });
    expect(document.getElementById("meResult").textContent).toContain('"success": true');
  });

  test("me button calls me api", async () => {
    const apiClient = jest.fn().mockResolvedValue({ success: true, data: { s_id: "32001041" } });
    bindApp(document, apiClient);

    fireEvent.click(screen.getByText("me"));
    await Promise.resolve();

    expect(apiClient).toHaveBeenCalledWith("/api/auth/me");
  });

  test("available button renders data", async () => {
    const apiClient = jest.fn().mockResolvedValue({ success: true, data: [{ roomId: "R101" }] });
    bindApp(document, apiClient);

    fireEvent.click(screen.getByText("available"));
    await Promise.resolve();

    expect(document.getElementById("availableResult").textContent).toContain("R101");
  });

  test("logout button uses POST", async () => {
    const apiClient = jest.fn().mockResolvedValue({ success: true, message: "logout success" });
    bindApp(document, apiClient);

    fireEvent.click(screen.getByText("logout"));
    await Promise.resolve();

    expect(apiClient).toHaveBeenCalledWith("/api/auth/logout", { method: "POST" });
  });

  test("login failure shows masked error", async () => {
    const apiClient = jest.fn().mockRejectedValue(new Error("Internal SQL Error"));
    bindApp(document, apiClient);

    document.getElementById("username").value = "student_01";
    document.getElementById("password").value = "123456";
    fireEvent.submit(document.getElementById("loginForm"));
    await Promise.resolve();

    const text = document.getElementById("meResult").textContent;
    expect(text).toContain('"success": false');
    expect(text).not.toContain("Internal SQL Error");
  });

  test("me button shows loading state first", async () => {
    const apiClient = jest.fn().mockImplementation(() => delayedResolve({ success: true, data: { name: "Alice" } }));
    bindApp(document, apiClient);

    fireEvent.click(screen.getByText("me"));
    expect(document.getElementById("meResult").textContent).toContain('"loading": true');
  });

  test("available failure shows generic error", async () => {
    const apiClient = jest.fn().mockRejectedValue(new Error("Network Error"));
    bindApp(document, apiClient);

    fireEvent.click(screen.getByText("available"));
    await Promise.resolve();

    const text = document.getElementById("availableResult").textContent;
    expect(text).toContain('"success": false');
    expect(text).not.toContain("Network Error");
  });

  test("logout button shows loading state first", async () => {
    const apiClient = jest.fn().mockImplementation(() => delayedResolve({ success: true, message: "ok" }));
    bindApp(document, apiClient);

    fireEvent.click(screen.getByText("logout"));
    expect(document.getElementById("logoutResult").textContent).toContain('"loading": true');
  });

  test("login lockout after five failed attempts", async () => {
    jest.useFakeTimers();
    const apiClient = jest.fn().mockRejectedValue(new Error("invalid"));
    bindApp(document, apiClient);

    document.getElementById("username").value = "student_01";
    document.getElementById("password").value = "123456";

    for (let i = 0; i < 5; i += 1) {
      fireEvent.submit(document.getElementById("loginForm"));
      await Promise.resolve();
    }

    fireEvent.submit(document.getElementById("loginForm"));
    const text = document.getElementById("meResult").textContent;
    expect(text).toContain('"success": false');
    expect(text).toContain("10");
    jest.useRealTimers();
  });
});