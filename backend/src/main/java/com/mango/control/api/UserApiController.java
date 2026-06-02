package com.mango.control.api;

import com.mango.constant.WebConstant;
import com.mango.dao.BaseDao;
import com.mango.pojo.BlackList;
import com.mango.pojo.Classroom;
import com.mango.pojo.Student;
import com.mango.service.Impl.BlackListServiceImpl;
import com.mango.service.Impl.ReservationServiceImpl;
import com.mango.service.Impl.StudentServiceImpl;
import com.mango.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApiController extends ApiControllerSupport {

    @Autowired
    private ReservationServiceImpl reservationService;

    @Autowired
    private StudentServiceImpl studentService;

    @Autowired
    private BlackListServiceImpl blackListService;

    @Autowired
    private BaseDao baseDao;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(HttpServletRequest request) {
        Student loginUser = requireLoginUser(request);
        Map<String, Object> map = new HashMap<>();
        map.put("s_id", loginUser.getS_id());
        int countTotalReservation = studentService.countReservation(map);
        map.put("state", WebConstant.RESERVATION_SUCCESS_STATE);
        int countSucReservation = studentService.countReservation(map);
        map.put("state", WebConstant.RESERVATION_CANCELED_STATE);
        int countCanceledReservation = studentService.countReservation(map);

        Map<String, Object> data = new HashMap<>();
        data.put("countClassroom", baseDao.countClassroom());
        data.put("countTotalReservation", countTotalReservation);
        data.put("countSucReservation", countSucReservation);
        data.put("countCanceledReservation", countCanceledReservation);
        data.put("students", reservationService.getAllStudentReservationInfo(loginUser));
        return ApiResponse.success(data);
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile(HttpServletRequest request) {
        Student loginUser = requireLoginUser(request);
        return ApiResponse.success(safeUserView(studentService.getStudentById(loginUser.getS_id())));
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Student loginUser = requireLoginUser(request);
        Student student = new Student();
        student.setS_id(loginUser.getS_id());

        String sName = getTrimmed(body, "s_name");
        String sYear = getTrimmed(body, "s_year");
        String sMajor = getTrimmed(body, "s_major");
        String sClass = getTrimmed(body, "s_class");
        String phone = getTrimmed(body, "s_phone_number");
        if (sName != null) {
            student.setS_name(sName);
        }
        if (sYear != null) {
            student.setS_year(sYear);
        }
        if (sMajor != null) {
            student.setS_major(sMajor);
        }
        if (sClass != null) {
            student.setS_class(sClass);
        }
        if (phone != null) {
            student.setS_phone_number(phone);
        }
        studentService.updateStudentInfo(student);
        return ApiResponse.success("profile updated", null);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Student loginUser = requireLoginUser(request);
        String oldPassword = getTrimmed(body, "old_password");
        String newPassword = getTrimmed(body, "new_password");
        String cmPassword = getTrimmed(body, "cm_password");
        if (oldPassword == null || newPassword == null || cmPassword == null) {
            throw new IllegalArgumentException("password fields are required");
        }
        Student currentUser = studentService.getStudentById(loginUser.getS_id());
        if (currentUser == null || !PasswordUtil.matches(oldPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("old password is wrong");
        }
        if (!newPassword.equals(cmPassword)) {
            throw new IllegalArgumentException("new passwords do not match");
        }
        studentService.updatePassword(loginUser.getS_id(), newPassword);
        loginUser.setPassword(PasswordUtil.hash(newPassword));
        return ApiResponse.success("password updated", null);
    }

    @GetMapping("/reservations")
    public ApiResponse<List<Student>> myReservations(HttpServletRequest request) {
        Student loginUser = requireLoginUser(request);
        return ApiResponse.success(reservationService.getAllStudentReservationInfo(loginUser));
    }

    @PostMapping("/reservations/cancel")
    public ApiResponse<Void> cancelReservation(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Student loginUser = requireLoginUser(request);
        String roomId = getTrimmed(body, "room_id");
        String timeId = getTrimmed(body, "time_id");
        String reservationDate = getTrimmed(body, "reservation_date");
        String buildingId = getTrimmed(body, "building_id");
        reservationService.updateDeleteReservationInfo(loginUser.getS_id(), roomId, timeId, reservationDate, buildingId);
        return ApiResponse.success("reservation canceled", null);
    }

    @GetMapping("/reservations/available")
    public ApiResponse<List<Classroom>> availableClassrooms(
            HttpServletRequest request,
            @RequestParam(value = "selectLocation", required = false) String selectLocation,
            @RequestParam(value = "selectBuildingName", required = false) String selectBuildingName,
            @RequestParam(value = "selectRoomName", required = false) String selectRoomName,
            @RequestParam(value = "selectRoomFloor", required = false) String selectRoomFloor,
            @RequestParam(value = "selectedTime", required = false) String selectedTime,
            @RequestParam(value = "selectDate", required = false) String selectDate) {
        requireLoginUser(request);
        Map<String, Object> map = new HashMap<>();
        if (selectLocation != null && !selectLocation.trim().isEmpty()) {
            map.put("selectLocation", selectLocation.trim());
        }
        if (selectBuildingName != null && !selectBuildingName.trim().isEmpty()) {
            map.put("selectBuildingName", selectBuildingName.trim());
        }
        if (selectRoomName != null && !selectRoomName.trim().isEmpty()) {
            map.put("selectRoomName", selectRoomName.trim());
        }
        if (selectRoomFloor != null && !selectRoomFloor.trim().isEmpty()) {
            map.put("selectRoomFloor", selectRoomFloor.trim());
        }

        String timeBegin = getTimeRangeStart(selectedTime);
        String timeEnd = getTimeRangeEnd(selectedTime);
        if (timeBegin != null && timeEnd != null) {
            map.put("time_begin", timeBegin);
            map.put("time_end", timeEnd);
        }
        fillDateRange(map, selectDate);

        return ApiResponse.success(reservationService.getAllAvailableClassroom(map));
    }

    @PostMapping("/reservations/submit")
    public ApiResponse<Void> submitReservation(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Student loginUser = requireLoginUser(request);

        BlackList blackedStudent = blackListService.getBlackedStudentById(loginUser.getS_id());
        if (blackedStudent != null) {
            throw new IllegalArgumentException("in blacklist, cannot reserve");
        }
        if (studentService.isThreeTimesCanceledOfWeekById(loginUser.getS_id())) {
            throw new IllegalArgumentException("canceled more than 3 times in one week");
        }

        Object selectedCheckboxObj = body == null ? null : body.get("selectedCheckbox");
        if (!(selectedCheckboxObj instanceof List)) {
            throw new IllegalArgumentException("selectedCheckbox format is invalid");
        }
        List<?> selectedCheckbox = (List<?>) selectedCheckboxObj;
        if (selectedCheckbox.isEmpty()) {
            throw new IllegalArgumentException("please select at least one time slot");
        }

        Map<String, Map<String, String>> roomIdAndTimeId = new HashMap<>();
        for (Object obj : selectedCheckbox) {
            if (obj == null) {
                continue;
            }
            String checkbox = obj.toString().trim();
            if (!checkbox.contains("-")) {
                continue;
            }
            String timeId = checkbox.substring(0, checkbox.indexOf("-"));
            String roomId = checkbox.substring(checkbox.indexOf("-") + 1);
            Map<String, String> pair = new HashMap<>();
            pair.put(roomId, timeId);
            roomIdAndTimeId.put(roomId.concat(timeId), pair);
        }
        if (roomIdAndTimeId.isEmpty()) {
            throw new IllegalArgumentException("selectedCheckbox content is invalid");
        }

        reservationService.setRoom_id_and_time_id(roomIdAndTimeId);
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("room_id_and_time_id", roomIdAndTimeId);
        List<Classroom> allSelectClassrooms = reservationService.getAllAvailableClassroom(queryMap);
        reservationService.setAllSelectClassrooms(allSelectClassrooms);
        reservationService.updateAddReservationInfo(loginUser.getS_id());
        return ApiResponse.success("reservation created", null);
    }

    @PostMapping("/reservations/preview")
    public ApiResponse<List<Classroom>> previewReservation(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        requireLoginUser(request);
        Object selectedCheckboxObj = body == null ? null : body.get("selectedCheckbox");
        if (!(selectedCheckboxObj instanceof List)) {
            return ApiResponse.success(new ArrayList<>());
        }
        List<?> selectedCheckbox = (List<?>) selectedCheckboxObj;
        Map<String, Map<String, String>> roomIdAndTimeId = new HashMap<>();
        for (Object obj : selectedCheckbox) {
            if (obj == null) {
                continue;
            }
            String checkbox = obj.toString().trim();
            if (!checkbox.contains("-")) {
                continue;
            }
            String timeId = checkbox.substring(0, checkbox.indexOf("-"));
            String roomId = checkbox.substring(checkbox.indexOf("-") + 1);
            Map<String, String> pair = new HashMap<>();
            pair.put(roomId, timeId);
            roomIdAndTimeId.put(roomId.concat(timeId), pair);
        }
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("room_id_and_time_id", roomIdAndTimeId);
        return ApiResponse.success(reservationService.getAllAvailableClassroom(queryMap));
    }
}
