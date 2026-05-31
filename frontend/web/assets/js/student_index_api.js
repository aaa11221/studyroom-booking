function formatTimeRange(reservation) {
    const info = reservation.roomAvailableTimeInfo || {};
    const table = info.timeTable || {};
    const begin = table.time_begin || "";
    const end = table.time_end || "";
    if (!begin && !end) {
        return "-";
    }
    return begin + "——" + end;
}

function renderDashboardRows(students) {
    const tbody = document.getElementById("dashboardTableBody");
    const rows = [];
    (students || []).forEach(function (student) {
        (student.studentReservations || []).forEach(function (reservation) {
            rows.push({
                s_name: student.s_name || "",
                s_id: reservation.s_id || "",
                room_name: reservation.room_name || "",
                building_id: reservation.building_id || "",
                time_range: formatTimeRange(reservation),
                reservation_date: reservation.reservation_date || ""
            });
        });
    });

    if (!rows.length) {
        tbody.innerHTML = "<tr><td colspan='6' class='text-center'>暂无预约记录</td></tr>";
        return;
    }

    tbody.innerHTML = rows.map(function (row) {
        return (
            "<tr>" +
            "<td>" + row.s_name + "</td>" +
            "<td>" + row.s_id + "</td>" +
            "<td>" + row.room_name + "</td>" +
            "<td>" + row.building_id + "</td>" +
            "<td>" + row.time_range + "</td>" +
            "<td>" + row.reservation_date + "</td>" +
            "</tr>"
        );
    }).join("");
}

function initDataTable() {
    if ($.fn.DataTable.isDataTable("#example1")) {
        $("#example1").DataTable().destroy();
    }
    $("#example1").DataTable({});
}

async function loadDashboard() {
    const result = await AppShell.api("/api/user/dashboard");
    const data = result.data || {};

    document.getElementById("countClassroom").textContent = data.countClassroom || 0;
    document.getElementById("countTotalReservation").textContent = data.countTotalReservation || 0;
    document.getElementById("countSucReservation").textContent = data.countSucReservation || 0;
    document.getElementById("countCanceledReservation").textContent = data.countCanceledReservation || 0;

    renderDashboardRows(data.students || []);
    initDataTable();
}

AppShell.initAuthPage()
    .then(function () {
        return loadDashboard();
    })
    .catch(function (error) {
        const tbody = document.getElementById("dashboardTableBody");
        if (tbody) {
            tbody.innerHTML = "<tr><td colspan='6' class='text-center text-danger'>加载失败：" + (error.message || "未知错误") + "</td></tr>";
        }
    });
