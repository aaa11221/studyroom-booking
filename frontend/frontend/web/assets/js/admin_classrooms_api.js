function initDataTable(selector) {
    if ($.fn.DataTable.isDataTable(selector)) {
        $(selector).DataTable().destroy();
    }
    $(selector).DataTable({});
}

function showClassroomError(err) {
    document.getElementById("classroomMsg").textContent = err.message || "操作失败";
}

function normalizeMultimedia(v) {
    if (v === "1" || v === 1 || v === "是") return "是";
    return "否";
}

function loadClassrooms() {
    return AppShell.api("/api/admin/classrooms").then(function (res) {
        const rows = res.data || [];
        const tbody = document.getElementById("classroomTableBody");
        if (!rows.length) {
            tbody.innerHTML = "<tr><td colspan='7' class='text-center'>暂无数据</td></tr>";
            return;
        }
        tbody.innerHTML = rows.map(function (c) {
            const roomId = c.room_id || "";
            return "<tr>" +
                "<td>" + (c.room_name || "") + "</td>" +
                "<td>" + roomId + "</td>" +
                "<td>" + (c.room_floor || "") + "</td>" +
                "<td>" + ((c.t_building && c.t_building.building_name) || c.building_id || "") + "</td>" +
                "<td>" + (c.available_seat || "") + "</td>" +
                "<td>" + normalizeMultimedia(c.is_multimedia_room) + "</td>" +
                "<td>" +
                "<a href='#' class='mr-3 text-primary edit-classroom' data-id='" + roomId + "'><span class='fas fa-pencil-alt'></span></a>" +
                "<a href='#' class='text-danger delete-classroom' data-id='" + roomId + "'><span class='fas fa-trash-alt'></span></a>" +
                "</td>" +
                "</tr>";
        }).join("");

        tbody.querySelectorAll(".delete-classroom").forEach(function (el) {
            el.addEventListener("click", function (e) {
                e.preventDefault();
                const roomId = el.getAttribute("data-id");
                if (!window.confirm("是否确认删除该教室")) return;
                AppShell.api("/api/admin/classrooms/" + encodeURIComponent(roomId), { method: "DELETE" })
                    .then(loadClassrooms)
                    .catch(showClassroomError);
            });
        });

        tbody.querySelectorAll(".edit-classroom").forEach(function (el) {
            el.addEventListener("click", function (e) {
                e.preventDefault();
                const roomId = el.getAttribute("data-id");
                const room_name = window.prompt("教室名称(可空跳过)");
                const room_floor = window.prompt("教室楼层(可空跳过)");
                const available_seat = window.prompt("座位数(可空跳过)");
                const is_multimedia_room = window.prompt("是否多媒体(是/否，可空跳过)");
                AppShell.api("/api/admin/classrooms/" + encodeURIComponent(roomId), {
                    method: "PUT",
                    body: JSON.stringify({
                        room_name: room_name,
                        room_floor: room_floor,
                        available_seat: available_seat,
                        is_multimedia_room: is_multimedia_room
                    })
                }).then(loadClassrooms).catch(showClassroomError);
            });
        });
        initDataTable("#example1");
    });
}

function bindClassroomActions() {
    document.getElementById("addClassroomBtn").addEventListener("click", function () {
        AppShell.api("/api/admin/classrooms", {
            method: "POST",
            body: JSON.stringify({
                room_id: document.getElementById("add_room_id").value.trim(),
                room_name: document.getElementById("add_room_name").value.trim(),
                room_floor: document.getElementById("add_room_floor").value.trim(),
                available_seat: document.getElementById("add_available_seat").value.trim(),
                building_id: document.getElementById("add_building_id").value.trim(),
                is_multimedia_room: document.getElementById("add_is_multimedia_room").value
            })
        }).then(loadClassrooms).catch(showClassroomError);
    });

    document.getElementById("addClassroomAvailableBtn").addEventListener("click", function () {
        AppShell.api("/api/admin/classrooms/available", {
            method: "POST",
            body: JSON.stringify({
                room_id: document.getElementById("av_room_id").value.trim(),
                time_id: document.getElementById("av_time_id").value.trim(),
                building_id: document.getElementById("av_building_id").value.trim(),
                available_date: document.getElementById("av_available_date").value.trim(),
                available_num: document.getElementById("av_available_num").value.trim()
            })
        }).then(function () {
            document.getElementById("classroomMsg").classList.remove("text-danger");
            document.getElementById("classroomMsg").classList.add("text-success");
            document.getElementById("classroomMsg").textContent = "可用时段添加成功";
        }).catch(showClassroomError);
    });
}

AppShell.initAdminPage()
    .then(function () {
        bindClassroomActions();
        return loadClassrooms();
    })
    .catch(showClassroomError);
