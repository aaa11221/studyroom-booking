package com.mango.service;

import com.mango.dao.BlackListDao;
import com.mango.dao.ClassroomDao;
import com.mango.dao.ReservationDao;
import com.mango.dao.StudentDao;
import com.mango.pojo.BlackList;
import com.mango.pojo.Classroom;
import com.mango.pojo.RoomAvailableTimeInfo;
import com.mango.pojo.Student;
import com.mango.pojo.StudentReservation;
import com.mango.service.Impl.BlackListServiceImpl;
import com.mango.service.Impl.ClassroomServiceImpl;
import com.mango.service.Impl.ReservationServiceImpl;
import com.mango.service.Impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceUnitTest {

    @Mock
    private StudentDao studentDao;

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ClassroomDao classroomDao;

    @Mock
    private BlackListDao blackListDao;

    @InjectMocks
    private StudentServiceImpl studentService;

    @InjectMocks
    private ClassroomServiceImpl classroomService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @InjectMocks
    private BlackListServiceImpl blackListService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(studentService, "studentDao", studentDao);
        ReflectionTestUtils.setField(studentService, "reservationDao", reservationDao);
        ReflectionTestUtils.setField(classroomService, "classroomDao", classroomDao);
        ReflectionTestUtils.setField(reservationService, "studentDao", studentDao);
        ReflectionTestUtils.setField(reservationService, "reservationDao", reservationDao);
        ReflectionTestUtils.setField(reservationService, "classroomDao", classroomDao);
        ReflectionTestUtils.setField(blackListService, "blackListDao", blackListDao);
    }

    @Test
    void getStudentByIdReturnsDaoResult() {
        Student expected = new Student();
        expected.setS_id("32001041");
        when(studentDao.getStudentById("32001041")).thenReturn(expected);

        Student actual = studentService.getStudentById("32001041");

        assertSame(expected, actual);
        verify(studentDao).getStudentById("32001041");
    }

    @Test
    void updatePasswordDelegatesToDao() {
        when(studentDao.updatePassword("32001041", "new-pass")).thenReturn(1);

        int updated = studentService.updatePassword("32001041", "new-pass");

        assertEquals(1, updated);
        verify(studentDao).updatePassword("32001041", "new-pass");
    }

    @Test
    void checkThreeCanceledReservationsWithinWeekReturnsTrue() {
        boolean result = studentService.checkThreeTimesCanceledOfWeek(Arrays.asList("20240601", "20240604", "20240607"));

        assertTrue(result);
    }

    @Test
    void checkThreeCanceledReservationsOutsideWeekReturnsFalse() {
        boolean result = studentService.checkThreeTimesCanceledOfWeek(Arrays.asList("20240601", "20240620", "20240710"));

        assertFalse(result);
    }

    @Test
    void isThreeTimesCanceledLoadsDatesFromReservationDao() {
        when(reservationDao.getAllCanceledReservationDateById("32001041"))
                .thenReturn(Arrays.asList("20240601", "20240602", "20240603"));

        boolean result = studentService.isThreeTimesCanceledOfWeekById("32001041");

        assertTrue(result);
        verify(reservationDao).getAllCanceledReservationDateById("32001041");
    }

    @Test
    void deleteStudentInfoRemovesStudentRelatedRows() {
        studentService.deleteStudentInfo("32001041");

        verify(studentDao).deleteStudentById("32001041");
        verify(studentDao).deleteStudentBlackListById("32001041");
        verify(studentDao).deleteStudentReservationById("32001041");
    }

    @Test
    void studentGetAllReturnsDaoResult() {
        List<Student> expected = Collections.singletonList(new Student());
        when(studentDao.getAll()).thenReturn(expected);

        assertSame(expected, studentService.getAll());
        verify(studentDao).getAll();
    }

    @Test
    void updateStudentInfoDelegatesToDao() {
        Student student = new Student();
        student.setS_id("32001041");
        when(studentDao.updateStudentInfo(student)).thenReturn(1);

        assertEquals(1, studentService.updateStudentInfo(student));
        verify(studentDao).updateStudentInfo(student);
    }

    @Test
    void countReservationDelegatesToDao() {
        Map<String, Object> query = new HashMap<>();
        query.put("s_id", "32001041");
        when(studentDao.countReservation(query)).thenReturn(3);

        assertEquals(3, studentService.countReservation(query));
        verify(studentDao).countReservation(query);
    }

    @Test
    void addStudentDelegatesToDao() {
        Student student = new Student();

        studentService.addStudent(student);

        verify(studentDao).addStudent(student);
    }

    @Test
    void deleteClassroomInfoRemovesClassroomRelatedRows() {
        classroomService.deleteClassroomInfo("R101");

        verify(classroomDao).deleteClassroom("R101");
        verify(classroomDao).deleteClassroomTimeTable("R101");
        verify(classroomDao).deleteClassroomAvailableTime("R101");
    }

    @Test
    void classroomGetAllReturnsDaoResult() {
        List<Classroom> expected = Collections.singletonList(new Classroom());
        when(classroomDao.getAll()).thenReturn(expected);

        assertSame(expected, classroomService.getAll());
        verify(classroomDao).getAll();
    }

    @Test
    void updateClassroomDelegatesToDao() {
        Map<String, Object> classroom = new HashMap<>();
        classroom.put("room_id", "R101");

        classroomService.updateClassroom(classroom);

        verify(classroomDao).updateClassroom(classroom);
    }

    @Test
    void addClassroomDelegatesToDao() {
        Classroom classroom = new Classroom();

        classroomService.addClassroom(classroom);

        verify(classroomDao).addClassroom(classroom);
    }

    @Test
    void addClassAvailableDelegatesToDao() {
        RoomAvailableTimeInfo slot = new RoomAvailableTimeInfo();

        classroomService.addClassAvailable(slot);

        verify(classroomDao).addClassroomAvailable(slot);
    }

    @Test
    void getClassroomReservedReturnsDaoCount() {
        when(classroomDao.getClassroomReserved("R101")).thenReturn(2);

        assertEquals(2, classroomService.getClassroomReserved("R101"));
        verify(classroomDao).getClassroomReserved("R101");
    }

    @Test
    void updateAddReservationInfoCreatesReservationAndUpdatesSeats() {
        RoomAvailableTimeInfo slot = new RoomAvailableTimeInfo("T1", "R101", "B1", "2024-06-01", "2", "8");
        Classroom classroom = new Classroom();
        classroom.setRoom_id("R101");
        classroom.setRoom_name("Room 101");
        classroom.setRoomAvailableTimeInfos(Collections.singletonList(slot));
        ReservationServiceImpl.setAllSelectClassrooms(Collections.singletonList(classroom));

        reservationService.updateAddReservationInfo("32001041");

        ArgumentCaptor<StudentReservation> reservationCaptor = ArgumentCaptor.forClass(StudentReservation.class);
        ArgumentCaptor<RoomAvailableTimeInfo> roomCaptor = ArgumentCaptor.forClass(RoomAvailableTimeInfo.class);
        verify(studentDao).addStudentReservation(reservationCaptor.capture());
        verify(classroomDao).updateRoomAvailableTimeInfo(roomCaptor.capture());
        assertEquals("32001041", reservationCaptor.getValue().getS_id());
        assertEquals("T1", reservationCaptor.getValue().getTime_id());
        assertEquals("R101", reservationCaptor.getValue().getRoom_id());
        assertEquals("3", roomCaptor.getValue().getReservation_num());
        assertEquals("7", roomCaptor.getValue().getAvailable_num());
    }

    @Test
    void updateDeleteReservationInfoMarksReservationAndRestoresSeat() {
        reservationService.updateDeleteReservationInfo("32001041", "R101", "T1", "2024-06-01", "B1");

        verify(studentDao).updateStudentReservationState(any(StudentReservation.class));
        verify(classroomDao).updateDeleteRoomAvailableSeatInfo(any(RoomAvailableTimeInfo.class));
    }

    @Test
    void reservationStudentInfoReturnsDaoResult() {
        Student student = new Student();
        List<Student> expected = Collections.singletonList(student);
        when(reservationDao.getAllStudentReservationInfo(student)).thenReturn(expected);

        assertSame(expected, reservationService.getAllStudentReservationInfo(student));
        verify(reservationDao).getAllStudentReservationInfo(student);
    }

    @Test
    void reservationClassroomInfoReturnsDaoResult() {
        Map<String, Object> query = new HashMap<>();
        List<Classroom> expected = Collections.singletonList(new Classroom());
        when(reservationDao.getAllClassroomReservationInfo(query)).thenReturn(expected);

        assertSame(expected, reservationService.getAllClassroomReservationInfo(query));
        verify(reservationDao).getAllClassroomReservationInfo(query);
    }

    @Test
    void blacklistServiceReturnsDaoResult() {
        BlackList blackList = new BlackList();
        when(blackListDao.getBlackedStudentById("32001041")).thenReturn(blackList);

        BlackList actual = blackListService.getBlackedStudentById("32001041");

        assertSame(blackList, actual);
        verify(blackListDao).getBlackedStudentById("32001041");
    }

    @Test
    void blacklistAddAndDeleteDelegateToDao() {
        BlackList blackList = new BlackList();

        blackListService.addStudentBlackList(blackList);
        blackListService.deleteStudentBlackList("32001041");

        verify(blackListDao).addStudentBlackList(blackList);
        verify(blackListDao).deleteStudentBlackList("32001041");
    }

    @Test
    void blacklistGetAllReturnsDaoResult() {
        List<Student> expected = Collections.singletonList(new Student());
        when(blackListDao.getAllBlackedStudent()).thenReturn(expected);

        assertSame(expected, blackListService.getAllBlackedStudent());
        verify(blackListDao).getAllBlackedStudent();
    }

    @Test
    void getAllAvailableClassroomPassesQueryMap() {
        Map<String, Object> query = new HashMap<>();
        query.put("room_id", "R101");
        List<Classroom> expected = Collections.singletonList(new Classroom());
        when(reservationDao.getAllAvailableClassroom(query)).thenReturn(expected);

        List<Classroom> actual = reservationService.getAllAvailableClassroom(query);

        assertSame(expected, actual);
        verify(reservationDao).getAllAvailableClassroom(query);
        verifyNoMoreInteractions(reservationDao);
    }
}
