package com.mango.control.api;

import com.mango.pojo.Student;
import com.mango.utils.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiControllerSupport {

    protected Student requireLoginUser(HttpServletRequest request) {
        Student user = CommonUtil.getLoginUser(request);
        if (user == null) {
            throw new SecurityException("not logged in");
        }
        return user;
    }

    protected Student requireAdmin(HttpServletRequest request) {
        Student user = requireLoginUser(request);
        if (!"admin".equals(user.getS_id())) {
            throw new SecurityException("admin role required");
        }
        return user;
    }

    protected String getTrimmed(Map<String, String> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) {
            return null;
        }
        String value = body.get(key).trim();
        return value.isEmpty() ? null : value;
    }

    protected String getTimeRangeStart(String selectedTime) {
        if (selectedTime == null || !selectedTime.contains("-")) {
            return null;
        }
        return selectedTime.substring(0, selectedTime.indexOf("-")).trim();
    }

    protected String getTimeRangeEnd(String selectedTime) {
        if (selectedTime == null || !selectedTime.contains("-")) {
            return null;
        }
        return selectedTime.substring(selectedTime.lastIndexOf("-") + 1).trim();
    }

    protected void fillDateRange(Map<String, Object> map, String selectDate) {
        if (selectDate == null || selectDate.trim().isEmpty()) {
            return;
        }
        String trimmed = selectDate.trim();
        int splitIndex = trimmed.indexOf(" ");
        int lastSplitIndex = trimmed.lastIndexOf(" ");
        if (splitIndex <= 0 || lastSplitIndex <= splitIndex) {
            return;
        }
        String dateBegin = CommonUtil.getDateFormat(trimmed.substring(0, splitIndex));
        String dateEnd = CommonUtil.getDateFormat(trimmed.substring(lastSplitIndex + 1));
        if (dateBegin != null && dateEnd != null && !dateBegin.equals(dateEnd)) {
            map.put("date_begin", dateBegin);
            map.put("date_end", dateEnd);
        }
    }

    protected Map<String, Object> safeUserView(Student student) {
        Map<String, Object> userView = new HashMap<>();
        if (student == null) {
            return userView;
        }
        userView.put("s_id", student.getS_id());
        userView.put("s_name", student.getS_name());
        userView.put("s_class", student.getS_class());
        userView.put("s_year", student.getS_year());
        userView.put("s_major", student.getS_major());
        userView.put("s_phone_number", student.getS_phone_number());
        userView.put("suc_num", student.getSuc_num());
        userView.put("canceled_num", student.getCanceled_num());
        userView.put("studentReservations", student.getStudentReservations());
        userView.put("blackList", student.getBlackList());
        return userView;
    }
}
