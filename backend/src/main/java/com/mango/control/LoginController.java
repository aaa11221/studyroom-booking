package com.mango.control;


import com.mango.constant.WebConstant;
import com.mango.dao.BaseDao;
import com.mango.pojo.Student;
import com.mango.service.Impl.StudentServiceImpl;
import com.mango.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 登录接口，进行登录判�?
 */
@Controller
public class LoginController {

    @Autowired
    StudentServiceImpl service;

    @Autowired
    BaseDao baseDao;

    @GetMapping({"/","login"})
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/logincheck")
    public String check(@RequestParam("username") String s_id, @RequestParam("password") String psw, Model model, HttpServletRequest request) {
        Student student = service.getStudentById(s_id);
        if (student == null) {
            model.addAttribute("msg","该用户不存在!");
            return "forward:/login.html";
        }else {
            if (PasswordUtil.matches(psw, student.getPassword())) {
                HttpSession session = request.getSession();
                request.changeSessionId();
                if (PasswordUtil.needsRehash(student.getPassword())) {
                    service.updatePassword(student.getS_id(), psw);
                    student.setPassword(PasswordUtil.hash(psw));
                }

                session.setAttribute(WebConstant.LOGIN_USER,student);
                if (student.getS_id().equals("admin")) {
                    return "redirect:index";
                }else {
                    return "redirect:student_index";
                }
            } else {
                model.addAttribute("msg","用户或密码错�?");
                return "forward:/login.html";
            }
        }
    }



}
