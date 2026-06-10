package com.mango.control.api;

import com.mango.constant.WebConstant;
import com.mango.dao.BaseDao;
import com.mango.pojo.BlackList;
import com.mango.pojo.Classroom;
import com.mango.pojo.RoomAvailableTimeInfo;
import com.mango.pojo.Student;
import com.mango.service.Impl.BlackListServiceImpl;
import com.mango.service.Impl.ClassroomServiceImpl;
import com.mango.service.Impl.ReservationServiceImpl;
import com.mango.service.Impl.StudentServiceImpl;
import com.mango.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController extends ApiControllerSupport {

    @Autowired
    private StudentServiceImpl studentService;

    @Autowired
    private ClassroomServiceImpl classroomService;

    @Autowired
    private ReservationServiceImpl reservationService;

    @Autowired
    private BlackListServiceImpl blackListService;

    @Autowired
    private BaseDao baseDao;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(HttpServletRequest request) {
        requireAdmin(request);
        Map<String, Object> data = new HashMap<>();
        data.put("countStudent", baseDao.countStudent());
        data.put("countClassroom", baseDao.countClassroom());
        data.put("countReservation", baseDao.countReservation());
        data.put("students", studentService.countStudentReservation());
        return ApiResponse.success(data);
    }

    @GetMapping("/students")
    public ApiResponse<List<Student>> students(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(studentService.getAll());
    }

    @PostMapping("/students")
    public ApiResponse<Void> addStudent(HttpServletRequest request, @RequestBody Map<String, String> body) {
        requireAdmin(request);
        String sId = getTrimmed(body, "s_id");
        String sName = getTrimmed(body, "s_name");
        String sClass = getTrimmed(body, "s_class");
        String sYear = getTrimmed(body, "s_year");
        String sMajor = getTrimmed(body, "s_major");
        String phone = getTrimmed(body, "s_phone_number");
        if (sId == null || sName == null) {
            throw new IllegalArgumentException("s_id and s_name are required");
        }
        studentService.addStudent(new Student(sId, sName, sClass, sYear, sMajor, phone));
        return ApiResponse.success("student created", null);
    }

    @DeleteMapping("/students/{sId}")
    public ApiResponse<Void> deleteStudent(HttpServletRequest request, @PathVariable("sId") String sId) {
        requireAdmin(request);
        studentService.deleteStudentInfo(sId);
        return ApiResponse.success("student deleted", null);
    }

    @GetMapping("/classrooms")
    public ApiResponse<List<Classroom>> classrooms(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(classroomService.getAll());
    }

    @PostMapping("/classrooms")
    public ApiResponse<Void> addClassroom(HttpServletRequest request, @RequestBody Map<String, String> body) {
        requireAdmin(request);
        String roomId = getTrimmed(body, "room_id");
        String roomName = getTrimmed(body, "room_name");
        String buildingId = getTrimmed(body, "building_id");
        String roomFloor = getTrimmed(body, "room_floor");
        String availableSeat = getTrimmed(body, "available_seat");
        String isMultimediaRoom = getTrimmed(body, "is_multimedia_room");
        if (roomId == null || roomName == null) {
            throw new IllegalArgumentException("room_id and room_name are required");
        }
        classroomService.addClassroom(
                new Classroom(roomId, roomName, buildingId, roomFloor, availableSeat, isMultimediaRoom)
        );
        return ApiResponse.success("classroom created", null);
    }

    @PutMapping("/classrooms/{roomId}")
    public ApiResponse<Void> updateClassroom(HttpServletRequest request,
                                             @PathVariable("roomId") String roomId,
                                             @RequestBody Map<String, String> body) {
        requireAdmin(request);
        Map<String, Object> map = new HashMap<>();
        map.put("room_id", roomId);
        String roomName = getTrimmed(body, "room_name");
        String roomFloor = getTrimmed(body, "room_floor");
        String availableSeat = getTrimmed(body, "available_seat");
        String isMultimediaRoom = getTrimmed(body, "is_multimedia_room");
        if (roomName != null) {
            map.put("room_name", roomName);
        }
        if (roomFloor != null) {
            map.put("room_floor", roomFloor);
        }
        if (availableSeat != null) {
            map.put("available_seat", availableSeat);
        }
        if (isMultimediaRoom != null) {
            map.put("is_multimedia_room", isMultimediaRoom);
        }
        classroomService.updateClassroom(map);
        return ApiResponse.success("classroom updated", null);
    }

    @DeleteMapping("/classrooms/{roomId}")
    public ApiResponse<Void> deleteClassroom(HttpServletRequest request, @PathVariable("roomId") String roomId) {
        requireAdmin(request);
        int reservedNums = classroomService.getClassroomReserved(roomId);
        if (reservedNums > 0) {
            throw new IllegalArgumentException("classroom already reserved");
        }
        classroomService.deleteClassroomInfo(roomId);
        return ApiResponse.success("classroom deleted", null);
    }

    @PostMapping("/classrooms/available")
    public ApiResponse<Void> addClassroomAvailable(HttpServletRequest request, @RequestBody Map<String, String> body) {
        requireAdmin(request);
        String timeId = getTrimmed(body, "time_id");
        String roomId = getTrimmed(body, "room_id");
        String buildingId = getTrimmed(body, "building_id");
        String availableDate = getTrimmed(body, "available_date");
        String availableNum = getTrimmed(body, "available_num");
        classroomService.addClassAvailable(new RoomAvailableTimeInfo(timeId, roomId, buildingId, availableDate, "0", availableNum));
        return ApiResponse.success("classroom availability created", null);
    }

    @GetMapping("/reservations/students")
    public ApiResponse<List<Student>> studentReservations(
            HttpServletRequest request,
            @RequestParam(value = "searchByIdOrName", required = false) String searchByIdOrName,
            @RequestParam(value = "selectedYear", required = false) String selectedYear,
            @RequestParam(value = "selectedMajor", required = false) String selectedMajor) {
        requireAdmin(request);
        Student student = new Student();
        if (searchByIdOrName != null && !searchByIdOrName.trim().isEmpty()) {
            student.setS_id(searchByIdOrName.trim());
        }
        if (selectedYear != null && !selectedYear.trim().isEmpty()) {
            student.setS_year(selectedYear.trim());
        }
        if (selectedMajor != null && !selectedMajor.trim().isEmpty()) {
            student.setS_major(selectedMajor.trim());
        }
        return ApiResponse.success(reservationService.getAllStudentReservationInfo(student));
    }

    @GetMapping("/reservations/classrooms")
    public ApiResponse<List<Classroom>> classroomReservations(
            HttpServletRequest request,
            @RequestParam(value = "searchByIdOrName", required = false) String searchByIdOrName,
            @RequestParam(value = "selectedTime", required = false) String selectedTime,
            @RequestParam(value = "selectDate", required = false) String selectDate) {
        requireAdmin(request);
        Map<String, Object> map = new HashMap<>();
        if (searchByIdOrName != null && !searchByIdOrName.trim().isEmpty()) {
            map.put("room_id", searchByIdOrName.trim());
        }
        String timeBegin = getTimeRangeStart(selectedTime);
        String timeEnd = getTimeRangeEnd(selectedTime);
        if (timeBegin != null && timeEnd != null) {
            map.put("time_begin", timeBegin);
            map.put("time_end", timeEnd);
        }
        fillDateRange(map, selectDate);
        return ApiResponse.success(reservationService.getAllClassroomReservationInfo(map));
    }

    @GetMapping("/blacklist")
    public ApiResponse<List<Student>> blacklist(HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.success(blackListService.getAllBlackedStudent());
    }

    @PostMapping("/blacklist")
    public ApiResponse<Void> addBlacklist(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Student admin = requireAdmin(request);
        String selectStudentId = getTrimmed(body, "selectStudentId");
        String selectDate = getTrimmed(body, "selectDate");
        String dateBegin = null;
        String dateEnd = null;
        if (selectDate != null && selectDate.contains(" ")) {
            dateBegin = CommonUtil.getDateFormat(selectDate.substring(0, selectDate.indexOf(" ")));
            dateEnd = CommonUtil.getDateFormat(selectDate.substring(selectDate.lastIndexOf(" ") + 1));
        }
        blackListService.addStudentBlackList(
                new BlackList(selectStudentId, dateBegin, dateEnd, WebConstant.BLACKED_SUCCESS_STATE, admin.getS_id())
        );
        return ApiResponse.success("blacklist created", null);
    }

    @DeleteMapping("/blacklist/{sId}")
    public ApiResponse<Void> deleteBlacklist(HttpServletRequest request, @PathVariable("sId") String sId) {
        requireAdmin(request);
        blackListService.deleteStudentBlackList(sId);
        return ApiResponse.success("blacklist removed", null);
    }
}
