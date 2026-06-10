function fillProfile(user) {
    document.getElementById("s_id").value = user.s_id || "";
    document.getElementById("s_name").value = user.s_name || "";
    document.getElementById("s_year").value = user.s_year || "";
    document.getElementById("s_major").value = user.s_major || "";
    document.getElementById("s_class").value = user.s_class || "";
    document.getElementById("s_phone_number").value = user.s_phone_number || "";
}

async function loadProfile() {
    const result = await AppShell.api("/api/user/profile");
    fillProfile(result.data || {});
}

function bindProfileSubmit() {
    const form = document.getElementById("profileForm");
    const msg = document.getElementById("profileMsg");
    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        msg.classList.remove("text-success");
        msg.classList.add("text-danger");
        msg.textContent = "";

        const payload = {
            s_name: document.getElementById("s_name").value,
            s_year: document.getElementById("s_year").value,
            s_major: document.getElementById("s_major").value,
            s_class: document.getElementById("s_class").value,
            s_phone_number: document.getElementById("s_phone_number").value
        };
        await AppShell.api("/api/user/profile", {
            method: "PUT",
            body: JSON.stringify(payload)
        });
        msg.classList.remove("text-danger");
        msg.classList.add("text-success");
        msg.textContent = "保存成功";
        await loadProfile();
        const me = await AppShell.loadMe();
        AppShell.applyUserToHeader(me);
    });
}

function bindPasswordSubmit() {
    const form = document.getElementById("passwordForm");
    const msg = document.getElementById("passwordMsg");
    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        msg.classList.remove("text-success");
        msg.classList.add("text-danger");
        msg.textContent = "";

        await AppShell.api("/api/user/password", {
            method: "PUT",
            body: JSON.stringify({
                old_password: document.getElementById("old_password").value,
                new_password: document.getElementById("new_password").value,
                cm_password: document.getElementById("cm_password").value
            })
        });
        msg.classList.remove("text-danger");
        msg.classList.add("text-success");
        msg.textContent = "密码修改成功";
        form.reset();
    });
}

AppShell.initAuthPage()
    .then(function () {
        bindProfileSubmit();
        bindPasswordSubmit();
        return loadProfile();
    })
    .catch(function (error) {
        document.getElementById("profileMsg").textContent = error.message || "加载失败";
    });
