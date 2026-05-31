(function initFrontend(globalObj) {
  const API_BASE = "";
  const LOGIN_MAX_ATTEMPTS = 5;
  const LOGIN_LOCK_MS = 10 * 60 * 1000;
  const loginGuard = {
    failedAttempts: 0,
    lockUntil: 0
  };

  function createApiClient(fetchImpl, baseUrl) {
    return async function request(path, options = {}) {
      const response = await fetchImpl(`${baseUrl}${path}`, {
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          ...(options.headers || {})
        },
        ...options
      });

      const data = await response.json();
      if (!response.ok || !data.success) {
        throw new Error(data.message || `HTTP ${response.status}`);
      }
      return data;
    };
  }

  function printJson(doc, id, value) {
    const el = doc.getElementById(id);
    if (el) {
      el.textContent = JSON.stringify(value, null, 2);
    }
  }

  function now() {
    return Date.now();
  }

  function validateCredentials(username, password) {
    const usernameRule = /^[A-Za-z0-9_@.-]{3,32}$/;
    if (!usernameRule.test(username)) {
      return {
        ok: false,
        message: "用户名格式不合法（3-32位，仅支持字母数字和_@.-）"
      };
    }
    if (password.length < 6 || password.length > 64) {
      return {
        ok: false,
        message: "密码长度需为6-64位"
      };
    }
    return { ok: true };
  }

  function getSafeErrorMessage(scope) {
    const fallback = "请求失败，请稍后重试";
    if (scope === "login") {
      return "登录失败，请检查账号或密码";
    }
    if (scope === "auth") {
      return "鉴权失败，请重新登录";
    }
    return fallback;
  }

  function bindApp(doc, apiClient) {
    const loginForm = doc.getElementById("loginForm");
    const meBtn = doc.getElementById("meBtn");
    const availableBtn = doc.getElementById("availableBtn");
    const logoutBtn = doc.getElementById("logoutBtn");

    if (!loginForm || !meBtn || !availableBtn || !logoutBtn) {
      return;
    }

    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        if (loginGuard.lockUntil > now()) {
          const remainingMin = Math.ceil((loginGuard.lockUntil - now()) / 60000);
          printJson(doc, "meResult", {
            success: false,
            message: `登录失败次数过多，请 ${remainingMin} 分钟后再试`
          });
          return;
        }

        const username = doc.getElementById("username").value.trim();
        const password = doc.getElementById("password").value.trim();
        const credentialCheck = validateCredentials(username, password);
        if (!credentialCheck.ok) {
          printJson(doc, "meResult", { success: false, message: credentialCheck.message });
          return;
        }

        printJson(doc, "meResult", { success: true, loading: true, message: "loading" });
        const result = await apiClient("/api/auth/login", {
          method: "POST",
          body: JSON.stringify({ username, password })
        });
        loginGuard.failedAttempts = 0;
        loginGuard.lockUntil = 0;
        printJson(doc, "meResult", result);
      } catch (error) {
        loginGuard.failedAttempts += 1;
        if (loginGuard.failedAttempts >= LOGIN_MAX_ATTEMPTS) {
          loginGuard.lockUntil = now() + LOGIN_LOCK_MS;
          loginGuard.failedAttempts = 0;
        }
        printJson(doc, "meResult", {
          success: false,
          message: getSafeErrorMessage("login")
        });
      }
    });

    meBtn.addEventListener("click", async () => {
      try {
        printJson(doc, "meResult", { success: true, loading: true, message: "loading" });
        const result = await apiClient("/api/auth/me");
        printJson(doc, "meResult", result);
      } catch (error) {
        printJson(doc, "meResult", {
          success: false,
          message: getSafeErrorMessage("auth")
        });
      }
    });

    availableBtn.addEventListener("click", async () => {
      try {
        printJson(doc, "availableResult", { success: true, loading: true, message: "loading" });
        const result = await apiClient("/api/user/reservations/available");
        printJson(doc, "availableResult", result);
      } catch (error) {
        printJson(doc, "availableResult", {
          success: false,
          message: getSafeErrorMessage("default")
        });
      }
    });

    logoutBtn.addEventListener("click", async () => {
      try {
        printJson(doc, "logoutResult", { success: true, loading: true, message: "loading" });
        const result = await apiClient("/api/auth/logout", { method: "POST" });
        printJson(doc, "logoutResult", result);
      } catch (error) {
        printJson(doc, "logoutResult", {
          success: false,
          message: getSafeErrorMessage("default")
        });
      }
    });
  }

  const exported = { createApiClient, printJson, bindApp, validateCredentials, getSafeErrorMessage };

  if (typeof module !== "undefined" && module.exports) {
    module.exports = exported;
  }

  if (globalObj.document && globalObj.fetch) {
    bindApp(globalObj.document, createApiClient(globalObj.fetch.bind(globalObj), API_BASE));
  }
})(typeof window !== "undefined" ? window : globalThis);
