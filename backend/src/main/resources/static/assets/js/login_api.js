async function loginApi(username, password) {
    const response = await fetch("/api/auth/login", {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    });
    const data = await response.json();
    if (!response.ok || !data.success) {
        throw new Error((data && data.message) ? data.message : "登录失败");
    }
    return data.data || {};
}

document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    const msg = document.getElementById("loginMsg");
    msg.textContent = "";
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    if (!username || !password) {
        msg.textContent = "用户名和密码不能为空";
        return;
    }
    try {
        const data = await loginApi(username, password);
        if (data.role === "admin") {
            window.location.href = "/index";
        } else {
            window.location.href = "/student_index";
        }
    } catch (error) {
        msg.textContent = error.message || "登录失败";
    }
});
