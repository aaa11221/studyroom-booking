function initDataTable(selector) {
    if ($.fn.DataTable.isDataTable(selector)) {
        $(selector).DataTable().destroy();
    }
    $(selector).DataTable({});
}

function toQuery(params) {
    const q = Object.keys(params).filter(function (k) {
        return params[k] && String(params[k]).trim() !== "";
    }).map(function (k) {
        return encodeURIComponent(k) + "=" + encodeURIComponent(params[k]);
    }).join("&");
    return q ? ("?" + q) : "";
}

function loadStudentReservations() {
    const query = toQuery({
        searchByIdOrName: document.getElementById("searchByIdOrName").value,
        selectedYear: document.getElementById("selectedYear").value,
        selectedMajor: document.getElementById("selectedMajor").value
    });
    return AppShell.api("/api/admin/reservations/students" + query).then(function (res) {
        const students = res.data || [];
        const rows = [];
        students.forEach(function (s) {
            (s.studentReservations || []).forEach(function (r) {
                const t = (r.roomAvailableTimeInfo && r.roomAvailableTimeInfo.timeTable) || {};
                rows.push({
                    s_name: s.s_name || "",
                    s_id: r.s_id || "",
                    room_name: r.room_name || "",
                    building_id: (r.roomAvailableTimeInfo && r.roomAvailableTimeInfo.building_id) || r.building_id || "",
                    time: (t.time_begin || "") + "—" + (t.time_end || ""),
                    date: r.reservation_date || "",
                    state: r.state || ""
                });
            });
        });
        const tbody = document.getElementById("studentReservationBody");
        if (!rows.length) {
            tbody.innerHTML = "<tr><td colspan='7' class='text-center'>暂无数据</td></tr>";
        } else {
            tbody.innerHTML = rows.map(function (r) {
                const cls = (r.state === "预约成功") ? "badge badge-success" : "badge badge-danger";
                return "<tr><td>" + r.s_name + "</td><td>" + r.s_id + "</td><td>" + r.room_name + "</td><td>" + r.building_id + "</td><td>" + r.time + "</td><td>" + r.date + "</td><td><span class='" + cls + "'>" + r.state + "</span></td></tr>";
            }).join("");
        }
        initDataTable("#example3");
    });
}

AppShell.initAdminPage()
    .then(function () {
        document.getElementById("studentReservationSearchForm").addEventListener("submit", function (e) {
            e.preventDefault();
            loadStudentReservations().catch(function () {});
        });
        return loadStudentReservations();
    })
    .catch(function () {});
