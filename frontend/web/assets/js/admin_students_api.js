function initDataTable(selector) {
    if ($.fn.DataTable.isDataTable(selector)) {
        $(selector).DataTable().destroy();
    }
    $(selector).DataTable({});
}

let adminStudents = [];

function renderStudents(students) {
    adminStudents = students || [];
    const tbody = document.getElementById("studentTableBody");
    if (!adminStudents.length) {
        tbody.innerHTML = "<tr><td colspan='7' class='text-center'>暂无数据</td></tr>";
        return;
    }
    tbody.innerHTML = adminStudents.map(function (s) {
        return "<tr>" +
            "<td>" + (s.s_id || "") + "</td>" +
            "<td>" + (s.s_name || "") + "</td>" +
            "<td>" + (s.s_class || "") + "</td>" +
            "<td>" + (s.s_major || "") + "</td>" +
            "<td>" + (s.s_year || "") + "</td>" +
            "<td>" + (s.s_phone_number || "") + "</td>" +
            "<td><a href='#' class='mr-4 text-danger delete-student' data-id='" + (s.s_id || "") + "'><span class='fas fa-trash-alt'></span></a></td>" +
            "</tr>";
    }).join("");
    tbody.querySelectorAll(".delete-student").forEach(function (el) {
        el.addEventListener("click", function (e) {
            e.preventDefault();
            const sid = el.getAttribute("data-id");
            if (!sid) return;
            if (!window.confirm("是否确认删除该学生")) return;
            AppShell.api("/api/admin/students/" + encodeURIComponent(sid), { method: "DELETE" })
                .then(loadStudents)
                .catch(showError);
        });
    });
}

function fillBlacklistStudentSelect() {
    const select = document.getElementById("bl_student_id");
    select.innerHTML = "<option value=''>选择学号</option>" + adminStudents.map(function (s) {
        return "<option value='" + (s.s_id || "") + "'>" + (s.s_id || "") + ": " + (s.s_name || "") + "</option>";
    }).join("");
}

function showError(err) {
    document.getElementById("studentMsg").textContent = err.message || "操作失败";
}

function loadStudents() {
    return AppShell.api("/api/admin/students").then(function (res) {
        renderStudents(res.data || []);
        fillBlacklistStudentSelect();
        initDataTable("#example1");
    });
}

function bindActions() {
    document.getElementById("addStudentBtn").addEventListener("click", function () {
        AppShell.api("/api/admin/students", {
            method: "POST",
            body: JSON.stringify({
                s_id: document.getElementById("add_s_id").value.trim(),
                s_name: document.getElementById("add_s_name").value.trim(),
                s_major: document.getElementById("add_s_major").value.trim(),
                s_class: document.getElementById("add_s_class").value.trim(),
                s_year: document.getElementById("add_s_year").value.trim(),
                s_phone_number: document.getElementById("add_s_phone_number").value.trim()
            })
        }).then(loadStudents).catch(showError);
    });

    document.getElementById("addBlacklistBtn").addEventListener("click", function () {
        AppShell.api("/api/admin/blacklist", {
            method: "POST",
            body: JSON.stringify({
                selectStudentId: document.getElementById("bl_student_id").value,
                selectDate: document.getElementById("bl_daterange").value
            })
        }).then(function () {
            document.getElementById("studentMsg").classList.remove("text-danger");
            document.getElementById("studentMsg").classList.add("text-success");
            document.getElementById("studentMsg").textContent = "黑名单设置成功";
        }).catch(showError);
    });
}

AppShell.initAdminPage()
    .then(function () {
        if (typeof $.fn.daterangepicker === "function") {
            $("#bl_daterange").daterangepicker({ locale: { format: "MM/DD/YYYY" } });
        }
        bindActions();
        return loadStudents();
    })
    .catch(showError);
