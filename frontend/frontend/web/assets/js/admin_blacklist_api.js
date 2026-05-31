function initDataTable(selector) {
    if ($.fn.DataTable.isDataTable(selector)) {
        $(selector).DataTable().destroy();
    }
    $(selector).DataTable({});
}

function showBlacklistError(err) {
    document.getElementById("blacklistMsg").textContent = err.message || "操作失败";
}

function loadBlacklist() {
    return AppShell.api("/api/admin/blacklist").then(function (res) {
        const list = res.data || [];
        const tbody = document.getElementById("blacklistBody");
        if (!list.length) {
            tbody.innerHTML = "<tr><td colspan='9' class='text-center'>暂无数据</td></tr>";
            initDataTable("#example3");
            return;
        }
        tbody.innerHTML = list.map(function (s) {
            const b = s.blackList || {};
            return "<tr>" +
                "<td>" + (s.s_id || "") + "</td>" +
                "<td>" + (s.s_name || "") + "</td>" +
                "<td>" + (s.s_class || "") + "</td>" +
                "<td>" + (s.s_major || "") + "</td>" +
                "<td>" + (s.s_year || "") + "</td>" +
                "<td>" + (b.date_begin || "") + "</td>" +
                "<td>" + (b.date_end || "") + "</td>" +
                "<td>" + (b.blacker_id || "") + "</td>" +
                "<td><a href='#' class='text-danger remove-blacklist' data-id='" + (s.s_id || "") + "'><span class='fas fa-trash-alt'></span></a></td>" +
                "</tr>";
        }).join("");
        tbody.querySelectorAll(".remove-blacklist").forEach(function (el) {
            el.addEventListener("click", function (e) {
                e.preventDefault();
                const sid = el.getAttribute("data-id");
                if (!window.confirm("确认将该学生移出黑名单吗？")) return;
                AppShell.api("/api/admin/blacklist/" + encodeURIComponent(sid), { method: "DELETE" })
                    .then(loadBlacklist)
                    .catch(showBlacklistError);
            });
        });
        initDataTable("#example3");
    });
}

AppShell.initAdminPage().then(loadBlacklist).catch(showBlacklistError);
