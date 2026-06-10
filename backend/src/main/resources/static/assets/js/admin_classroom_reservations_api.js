function initDataTable(selector) {
    if ($.fn.DataTable.isDataTable(selector)) {
        $(selector).DataTable().destroy();
    }
    $(selector).DataTable({});
}

function q(params) {
    const s = Object.keys(params).filter(function (k) {
        return params[k] && String(params[k]).trim() !== "";
    }).map(function (k) {
        return encodeURIComponent(k) + "=" + encodeURIComponent(params[k]);
    }).join("&");
    return s ? ("?" + s) : "";
}

function toUseRate(reservationNum, availableSeat) {
    const r = Number(reservationNum || 0);
    const a = Number(availableSeat || 0);
    if (!r && !a) return "0.00%";
    return ((r / (r + a)) * 100).toFixed(2) + "%";
}

function loadClassroomReservations() {
    const query = q({
        searchByIdOrName: document.getElementById("searchByIdOrName").value,
        selectedTime: document.getElementById("selectedTime").value,
        selectDate: document.getElementById("daterange").value
    });
    return AppShell.api("/api/admin/reservations/classrooms" + query).then(function (res) {
        const classrooms = res.data || [];
        const rows = [];
        classrooms.forEach(function (c) {
            (c.studentReservations || []).forEach(function (sr) {
                const info = sr.roomAvailableTimeInfo || {};
                const table = info.timeTable || {};
                rows.push({
                    room_name: c.room_name || "",
                    room_floor: c.room_floor || "",
                    building_name: (c.t_building && c.t_building.building_name) || "",
                    time_range: (table.time_begin || "") + "——" + (table.time_end || ""),
                    reservation_date: sr.reservation_date || "",
                    reservation_num: info.reservation_num || "0",
                    available_seat: c.available_seat || "0",
                    use_rate: toUseRate(info.reservation_num, c.available_seat)
                });
            });
        });
        const tbody = document.getElementById("classroomReservationBody");
        if (!rows.length) {
            tbody.innerHTML = "<tr><td colspan='8' class='text-center'>暂无数据</td></tr>";
        } else {
            tbody.innerHTML = rows.map(function (r) {
                return "<tr><td>" + r.room_name + "</td><td>" + r.room_floor + "</td><td>" + r.building_name + "</td><td>" + r.time_range + "</td><td>" + r.reservation_date + "</td><td>" + r.reservation_num + "</td><td>" + r.available_seat + "</td><td>" + r.use_rate + "</td></tr>";
            }).join("");
        }
        initDataTable("#example1");
    });
}

AppShell.initAdminPage()
    .then(function () {
        if (typeof $.fn.daterangepicker === "function") {
            $("#daterange").daterangepicker({ locale: { format: "MM/DD/YYYY" } });
        }
        document.getElementById("classroomReservationSearchForm").addEventListener("submit", function (e) {
            e.preventDefault();
            loadClassroomReservations().catch(function () {});
        });
        return loadClassroomReservations();
    })
    .catch(function () {});
