let currentRows = [];

function getFilters() {
    return {
        selectLocation: document.getElementById("selectLocation").value,
        selectBuildingName: document.getElementById("selectBuildingName").value,
        selectRoomName: document.getElementById("selectRoomName").value,
        selectRoomFloor: document.getElementById("selectRoomFloor").value,
        selectedTime: document.getElementById("selectTime").value,
        selectDate: document.getElementById("daterange").value
    };
}

function toQueryString(params) {
    const query = Object.keys(params)
        .filter(function (k) {
            return params[k] !== undefined && params[k] !== null && String(params[k]).trim() !== "";
        })
        .map(function (k) {
            return encodeURIComponent(k) + "=" + encodeURIComponent(params[k]);
        })
        .join("&");
    return query ? ("?" + query) : "";
}

function formatTimeRange(info) {
    const table = (info && info.timeTable) ? info.timeTable : {};
    const begin = table.time_begin || "";
    const end = table.time_end || "";
    return begin && end ? (begin + "——" + end) : "-";
}

function initDataTable() {
    if ($.fn.DataTable.isDataTable("#example1")) {
        $("#example1").DataTable().destroy();
    }
    $("#example1").DataTable({});
}

function renderRows(classrooms) {
    const tbody = document.getElementById("availableTableBody");
    const rows = [];
    (classrooms || []).forEach(function (room) {
        (room.roomAvailableTimeInfos || []).forEach(function (slot) {
            rows.push({
                building_location: room.t_building ? room.t_building.building_location : "",
                building_name: room.t_building ? room.t_building.building_name : "",
                room_name: room.room_name || "",
                room_floor: room.room_floor || "",
                time_range: formatTimeRange(slot),
                available_date: slot.available_date || "",
                available_num: slot.available_num || 0,
                checkbox_value: (slot.time_id || "") + "-" + (slot.room_id || ""),
                room_id: slot.room_id || "",
                time_id: slot.time_id || ""
            });
        });
    });

    currentRows = rows;
    if (!rows.length) {
        tbody.innerHTML = "<tr><td colspan='8' class='text-center'>暂无可预约记录</td></tr>";
        return;
    }

    tbody.innerHTML = rows.map(function (row, idx) {
        return (
            "<tr>" +
            "<td>" + row.building_location + "</td>" +
            "<td>" + row.building_name + "</td>" +
            "<td>" + row.room_name + "</td>" +
            "<td>" + row.room_floor + "</td>" +
            "<td>" + row.time_range + "</td>" +
            "<td>" + row.available_date + "</td>" +
            "<td>" + row.available_num + "</td>" +
            "<td><input type='checkbox' class='checkbox reservation-check' data-row-index='" + idx + "'></td>" +
            "</tr>"
        );
    }).join("");
}

async function searchAvailable() {
    const msg = document.getElementById("makeReservationMsg");
    msg.textContent = "";
    const query = toQueryString(getFilters());
    const result = await AppShell.api("/api/user/reservations/available" + query);
    renderRows(result.data || []);
    initDataTable();
}

function getSelectedCheckboxValues() {
    const selected = [];
    document.querySelectorAll(".reservation-check:checked").forEach(function (el) {
        const idx = Number(el.getAttribute("data-row-index"));
        if (!Number.isNaN(idx) && currentRows[idx]) {
            selected.push(currentRows[idx].checkbox_value);
        }
    });
    return selected;
}

async function submitReservation() {
    const msg = document.getElementById("makeReservationMsg");
    msg.textContent = "";
    const selectedCheckbox = getSelectedCheckboxValues();
    if (!selectedCheckbox.length) {
        msg.textContent = "请先勾选要预约的时段";
        return;
    }

    await AppShell.api("/api/user/reservations/submit", {
        method: "POST",
        body: JSON.stringify({
            selectedCheckbox: selectedCheckbox
        })
    });
    msg.classList.remove("text-danger");
    msg.classList.add("text-success");
    msg.textContent = "预约成功";
    await searchAvailable();
}

function bindActions() {
    const searchBtn = document.getElementById("searchBtn");
    const reserveBtn = document.getElementById("reserveBtn");
    searchBtn.addEventListener("click", function () {
        searchAvailable().catch(function (error) {
            const msg = document.getElementById("makeReservationMsg");
            msg.classList.remove("text-success");
            msg.classList.add("text-danger");
            msg.textContent = error.message || "搜索失败";
        });
    });
    reserveBtn.addEventListener("click", function () {
        submitReservation().catch(function (error) {
            const msg = document.getElementById("makeReservationMsg");
            msg.classList.remove("text-success");
            msg.classList.add("text-danger");
            msg.textContent = error.message || "预约失败";
        });
    });
}

function initDateRange() {
    if (typeof $.fn.daterangepicker === "function") {
        $("#daterange").daterangepicker({
            locale: {
                format: "MM/DD/YYYY"
            }
        });
    }
}

AppShell.initAuthPage()
    .then(function () {
        initDateRange();
        bindActions();
        return searchAvailable();
    })
    .catch(function (error) {
        const tbody = document.getElementById("availableTableBody");
        if (tbody) {
            tbody.innerHTML = "<tr><td colspan='8' class='text-center text-danger'>加载失败：" + (error.message || "未知错误") + "</td></tr>";
        }
    });
