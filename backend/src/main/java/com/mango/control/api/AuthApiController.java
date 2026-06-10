package com.mango.control.api;

import com.mango.constant.WebConstant;
import com.mango.pojo.Student;
import com.mango.service.Impl.StudentServiceImpl;
import com.mango.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController extends ApiControllerSupport {

    @Autowired
    private StudentServiceImpl studentService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = getTrimmed(body, "username");
        String password = getTrimmed(body, "password");
        if (username == null || password == null) {
            throw new IllegalArgumentException("username and password are required");
        }

        Student student = studentService.getStudentById(username);
        if (student == null || !PasswordUtil.matches(password, student.getPassword())) {
            throw new IllegalArgumentException("username or password is invalid");
        }

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        if (PasswordUtil.needsRehash(student.getPassword())) {
            studentService.updatePassword(student.getS_id(), password);
            student.setPassword(PasswordUtil.hash(password));
        }
        session.setAttribute(WebConstant.LOGIN_USER, student);

        Map<String, Object> data = new HashMap<>();
        data.put("role", "admin".equals(student.getS_id()) ? "admin" : "student");
        data.put("user", safeUserView(student));
        return ApiResponse.success("login success", data);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(HttpServletRequest request) {
        Student user = requireLoginUser(request);
        Map<String, Object> data = new HashMap<>();
        data.put("role", "admin".equals(user.getS_id()) ? "admin" : "student");
        data.put("user", safeUserView(studentService.getStudentById(user.getS_id())));
        return ApiResponse.success(data);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.success("logout success", null);
    }
}
