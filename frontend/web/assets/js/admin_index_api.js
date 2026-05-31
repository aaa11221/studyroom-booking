function initDataTable(selector) {
    if ($.fn.DataTable.isDataTable(selector)) {
        $(selector).DataTable().destroy();
    }
    $(selector).DataTable({});
}

function renderAdminDashboardRows(students) {
    const tbody = document.getElementById("adminDashboardBody");
    if (!students || !students.length) {
        tbody.innerHTML = "<tr><td colspan='8' class='text-center'>暂无数据</td></tr>";
        return;
    }
    tbody.innerHTML = students.map(function (s) {
        return "<tr>" +
            "<td>" + (s.s_name || "") + "</td>" +
            "<td>" + (s.s_id || "") + "</td>" +
            "<td>" + (s.s_major || "") + "</td>" +
            "<td>" + (s.s_class || "") + "</td>" +
            "<td>" + (s.s_year || "") + "</td>" +
            "<td>" + (s.s_phone_number || "") + "</td>" +
            "<td>" + (s.suc_num || "0") + "</td>" +
            "<td>" + (s.canceled_num || "0") + "</td>" +
            "</tr>";
    }).join("");
}

AppShell.initAdminPage()
    .then(function () { return AppShell.api("/api/admin/dashboard"); })
    .then(function (res) {
        const d = res.data || {};
        document.getElementById("countStudent").textContent = d.countStudent || 0;
        document.getElementById("countClassroom").textContent = d.countClassroom || 0;
        document.getElementById("countReservation").textContent = d.countReservation || 0;
        renderAdminDashboardRows(d.students || []);
        initDataTable("#example1");
    })
    .catch(function (err) {
        const tbody = document.getElementById("adminDashboardBody");
        if (tbody) {
            tbody.innerHTML = "<tr><td colspan='8' class='text-center text-danger'>" + (err.message || "加载失败") + "</td></tr>";
        }
    });
