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

function stateBadgeClass(state) {
    if (!state) {
        return "badge badge-secondary";
    }
    const value = String(state).toLowerCase();
    if (value.includes("success") || value.includes("成功")) {
        return "badge badge-success";
    }
    return "badge badge-danger";
}

function flattenReservations(students) {
    const rows = [];
    (students || []).forEach(function (student) {
        const list = student.studentReservations || [];
        list.forEach(function (reservation) {
            rows.push({
                s_name: student.s_name || "",
                s_id: reservation.s_id || "",
                room_name: reservation.room_name || "",
                building_id: reservation.building_id || "",
                time_range: formatTimeRange(reservation),
                reservation_date: reservation.reservation_date || "",
                state: reservation.state || "",
                room_id: reservation.room_id || "",
                time_id: reservation.time_id || ""
            });
        });
    });
    return rows;
}

async function cancelReservation(row) {
    const ok = window.confirm("是否取消预约？");
    if (!ok) {
        return;
    }
    await AppShell.api("/api/user/reservations/cancel", {
        method: "POST",
        body: JSON.stringify({
            room_id: row.room_id,
            time_id: row.time_id,
            reservation_date: row.reservation_date,
            building_id: row.building_id
        })
    });
    await loadReservations();
}

function initDataTable() {
    if ($.fn.DataTable.isDataTable("#example1")) {
        $("#example1").DataTable().destroy();
    }
    $("#example1").DataTable({});
}

function renderRows(rows) {
    const tbody = document.getElementById("reservationTableBody");
    if (!rows.length) {
        tbody.innerHTML = "<tr><td colspan='7' class='text-center'>暂无预约记录</td></tr>";
        return;
    }

    tbody.innerHTML = rows.map(function (row, idx) {
        const badgeClass = stateBadgeClass(row.state);
        return (
            "<tr>" +
            "<td>" + row.s_name + "</td>" +
            "<td>" + row.s_id + "</td>" +
            "<td>" + row.room_name + "</td>" +
            "<td>" + row.building_id + "</td>" +
            "<td>" + row.time_range + "</td>" +
            "<td>" + row.reservation_date + "</td>" +
            "<td class='text-start'>" +
            "<span class='" + badgeClass + "'>" + row.state + "</span> " +
            "<span class='badge badge-warning edit' data-row-index='" + idx + "' style='cursor:pointer;'>取消预约</span>" +
            "</td>" +
            "</tr>"
        );
    }).join("");

    tbody.querySelectorAll(".edit").forEach(function (el) {
        el.addEventListener("click", function () {
            const index = Number(el.getAttribute("data-row-index"));
            cancelReservation(rows[index]).catch(function (error) {
                alert(error.message || "取消预约失败");
            });
        });
    });
}

async function loadReservations() {
    const result = await AppShell.api("/api/user/reservations");
    const rows = flattenReservations(result.data);
    renderRows(rows);
    initDataTable();
}

AppShell.initAuthPage()
    .then(function () {
        return loadReservations();
    })
    .catch(function (error) {
        const tbody = document.getElementById("reservationTableBody");
        if (tbody) {
            tbody.innerHTML = "<tr><td colspan='7' class='text-center text-danger'>加载失败：" + (error.message || "未知错误") + "</td></tr>";
        }
    });
