(function (window) {
    async function api(path, options) {
        const response = await fetch(path, {
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            ...(options || {})
        });

        let data = null;
        try {
            data = await response.json();
        } catch (e) {
            data = null;
        }

        if (!response.ok || (data && data.success === false)) {
            const message = (data && data.message) ? data.message : ("HTTP " + response.status);
            throw new Error(message);
        }
        return data || {};
    }

    async function loadMe() {
        const result = await api("/api/auth/me");
        return result.data || {};
    }

    function applyUserToHeader(data) {
        const user = data.user || {};
        const role = data.role || "student";
        const userNameEl = document.getElementById("headerUserName");
        const userRoleEl = document.getElementById("headerUserRole");
        if (userNameEl) {
            userNameEl.textContent = user.s_name || "未登录";
        }
        if (userRoleEl) {
            userRoleEl.textContent = role === "admin" ? "管理员" : "学生";
        }
        const welcomeName = document.getElementById("welcomeName");
        if (welcomeName) {
            welcomeName.textContent = user.s_name || "";
        }
    }

    function bindLogout() {
        const logoutBtn = document.getElementById("logoutBtn");
        if (!logoutBtn) {
            return;
        }
        logoutBtn.addEventListener("click", async function (e) {
            e.preventDefault();
            try {
                await api("/api/auth/logout", { method: "POST" });
            } catch (err) {
                // ignore
            }
            window.location.href = "/login.html";
        });
    }

    async function initAuthPage() {
        bindLogout();
        try {
            const me = await loadMe();
            applyUserToHeader(me);
            return me;
        } catch (error) {
            window.location.href = "/login.html";
            throw error;
        }
    }

    async function initAdminPage() {
        const me = await initAuthPage();
        if (me.role !== "admin") {
            window.location.href = "/student_index.html";
            throw new Error("admin role required");
        }
        return me;
    }

    window.AppShell = {
        api: api,
        loadMe: loadMe,
        applyUserToHeader: applyUserToHeader,
        bindLogout: bindLogout,
        initAuthPage: initAuthPage,
        initAdminPage: initAdminPage
    };
})(window);
