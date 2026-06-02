package com.mango.control.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mango.constant.WebConstant;
import com.mango.dao.BaseDao;
import com.mango.monitoring.MonitoringMetrics;
import com.mango.pojo.BlackList;
import com.mango.pojo.Classroom;
import com.mango.pojo.Student;
import com.mango.service.Impl.BlackListServiceImpl;
import com.mango.service.Impl.ClassroomServiceImpl;
import com.mango.service.Impl.ReservationServiceImpl;
import com.mango.service.Impl.StudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthApiController.class, UserApiController.class, AdminApiController.class})
@Import(ApiExceptionHandler.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentServiceImpl studentService;

    @MockBean
    private ClassroomServiceImpl classroomService;

    @MockBean
    private ReservationServiceImpl reservationService;

    @MockBean
    private BlackListServiceImpl blackListService;

    @MockBean
    private BaseDao baseDao;

    @MockBean
    private MonitoringMetrics monitoringMetrics;

    @Test
    void loginSuccessReturnsSafeUserAndRole() throws Exception {
        Student student = student("32001041", "Alice", "123456");
        when(studentService.getStudentById("32001041")).thenReturn(student);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "32001041", "password", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.role", is("student")))
                .andExpect(jsonPath("$.data.user.s_name", is("Alice")))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void loginWrongPasswordReturnsBadRequest() throws Exception {
        when(studentService.getStudentById("32001041")).thenReturn(student("32001041", "Alice", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("username", "32001041", "password", "wrong")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void loginMissingUsernameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("password", "123456")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("username and password are required")));
    }

    @Test
    void meWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("not logged in")));
    }

    @Test
    void profileReturnsLoggedInUser() throws Exception {
        Student loginUser = student("32001041", "Alice", "123456");
        when(studentService.getStudentById("32001041")).thenReturn(loginUser);

        mockMvc.perform(get("/api/user/profile")
                        .sessionAttr(WebConstant.LOGIN_USER, loginUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.s_id", is("32001041")))
                .andExpect(jsonPath("$.data.s_name", is("Alice")));
    }

    @Test
    void changePasswordRejectsWrongOldPassword() throws Exception {
        Student loginUser = student("32001041", "Alice", "123456");

        mockMvc.perform(put("/api/user/password")
                        .sessionAttr(WebConstant.LOGIN_USER, loginUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("old_password", "bad", "new_password", "654321", "cm_password", "654321")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("old password is wrong")));

        verify(studentService, never()).updatePassword(any(), any());
    }

    @Test
    void adminStudentsRequiresAdminRole() throws Exception {
        Student loginUser = student("32001041", "Alice", "123456");

        mockMvc.perform(get("/api/admin/students")
                        .sessionAttr(WebConstant.LOGIN_USER, loginUser))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("admin role required")));
    }

    @Test
    void adminAddStudentValidatesRequiredFields() throws Exception {
        Student admin = student("admin", "Admin", "admin-pass");

        mockMvc.perform(post("/api/admin/students")
                        .sessionAttr(WebConstant.LOGIN_USER, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("s_id", "32001041")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("s_id and s_name are required")));
    }

    @Test
    void adminClassroomsReturnsList() throws Exception {
        Student admin = student("admin", "Admin", "admin-pass");
        Classroom classroom = new Classroom("R101", "Room 101", "B1", "1", "40", "yes");
        when(classroomService.getAll()).thenReturn(Collections.singletonList(classroom));

        mockMvc.perform(get("/api/admin/classrooms")
                        .sessionAttr(WebConstant.LOGIN_USER, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].room_id", is("R101")));
    }

    @Test
    void submitReservationRejectsBlacklistedStudent() throws Exception {
        Student loginUser = student("32001041", "Alice", "123456");
        when(blackListService.getBlackedStudentById("32001041")).thenReturn(new BlackList());

        Map<String, Object> body = new HashMap<>();
        body.put("selectedCheckbox", Collections.singletonList("T1-R101"));
        mockMvc.perform(post("/api/user/reservations/submit")
                        .sessionAttr(WebConstant.LOGIN_USER, loginUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("in blacklist, cannot reserve")));
    }

    private Student student(String id, String name, String password) {
        Student student = new Student();
        student.setS_id(id);
        student.setS_name(name);
        student.setPassword(password);
        return student;
    }

    private String json(String key, String value) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put(key, value);
        return objectMapper.writeValueAsString(body);
    }

    private String json(String key1, String value1, String key2, String value2) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put(key1, value1);
        body.put(key2, value2);
        return objectMapper.writeValueAsString(body);
    }

    private String json(String key1, String value1, String key2, String value2, String key3, String value3) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put(key1, value1);
        body.put(key2, value2);
        body.put(key3, value3);
        return objectMapper.writeValueAsString(body);
    }
}
