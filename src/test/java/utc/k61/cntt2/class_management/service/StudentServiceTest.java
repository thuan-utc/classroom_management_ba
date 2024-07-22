package utc.k61.cntt2.class_management.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.domain.Role;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.StudentDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ClassAttendanceRepository;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;
import utc.k61.cntt2.class_management.repository.TutorFeeDetailRepository;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudentServiceTest {

    private ClassRegistrationRepository classRegistrationRepository;
    private UserService userService;

    private StudentService studentService;
    @Before
    public void setUp() {
        classRegistrationRepository = mock(ClassRegistrationRepository.class);
        ClassAttendanceRepository classAttendanceRepository = mock(ClassAttendanceRepository.class);
        TutorFeeDetailRepository tutorFeeDetailRepository = mock(TutorFeeDetailRepository.class);
        ClassroomRepository classroomRepository = mock(ClassroomRepository.class);
        userService = mock(UserService.class);
        ClassroomService classroomService = mock(ClassroomService.class);
        String tempFolder = "/temp";

        studentService = new StudentService(classRegistrationRepository, classAttendanceRepository, tutorFeeDetailRepository, classroomRepository,userService, classroomService, tempFolder);
    }

    @Test(expected = BusinessException.class)
    public void shouldThrowExceptionWhenDeleteStudentWithMissingPermission(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);

        studentService.deleteStudent(1L);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowExceptionWhenDeleteStudentWithNotFoundStudent(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        user.setRole(role);
        Classroom classroom = new Classroom();

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setId(1L);

        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classRegistrations.add(classRegistration);
        classroom.setClassRegistrations(classRegistrations);

        List <Classroom> classrooms = new ArrayList<>();
        classrooms.add(classroom);

        user.setClassrooms(classrooms);

        when(userService.getCurrentUserLogin()).thenReturn(user);

        studentService.deleteStudent(2L);
    }

    @Test
    public void shouldReturnApiResponseWhenDeleteStudent(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        user.setRole(role);
        Classroom classroom = new Classroom();

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setId(1L);

        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classRegistrations.add(classRegistration);
        classroom.setClassRegistrations(classRegistrations);

        List <Classroom> classrooms = new ArrayList<>();
        classrooms.add(classroom);

        user.setClassrooms(classrooms);

        when(userService.getCurrentUserLogin()).thenReturn(user);

        studentService.deleteStudent(1L);
    }



    // new test
    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowExceptionWhenGetStudentDetailWitnNotFoundStudent() {
        studentService.getStudentDetail(1L);
    }


    @Test
    public void shouldReturnDataWhenGetStudentDetail() {
        ClassRegistration student = new ClassRegistration();
        student.setId(1L);
        student.setSurname("Hoang");
        student.setFirstName("Nguyen");
        student.setLastName("Van");
        student.setEmail("van@gmail.com");
        student.setPhone("0522183692");

        when(classRegistrationRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentDto studentDto = (StudentDto) studentService.getStudentDetail(1L);
        assertEquals("0522183692", studentDto.getPhone());
    }


    @Test (expected = BusinessException.class)
    public void shouldThrowExceptionWhenSearchWithRequireRoleTeacher() {
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        User user = new User();
        user.setRole(role);
        
        Map<String,String> params = new HashMap<>();

        when(userService.getCurrentUserLogin()).thenReturn(user);
        studentService.search(params, null);
    }

    @Test
    public void shouldReturnDataWhenSearch() {
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        User user = new User();
        user.setRole(role);

        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setClassName("cntt");
        List<Classroom> classrooms = new ArrayList<>();
        classrooms.add(classroom);
        user.setClassrooms(classrooms);

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setId(2L);
        classRegistration.setFirstName("Le");
        classRegistration.setSurname("Long");
        classRegistration.setLastName("Hoang");
        classRegistration.setEmail("Hoang@gmail.com");
        Classroom classroom1 = new Classroom();
        classroom1.setId(1L);
        classroom1.setClassName("cntt");
        classRegistration.setClassroom(classroom1);
        List<ClassRegistration>  classRegistrationList = new ArrayList<>();
        classRegistrationList.add(classRegistration);
        Page<ClassRegistration> page = new PageImpl<>(classRegistrationList);

        Map<String,String> params = new HashMap<>();
        params.put("firstName", "Le");
        params.put("surname", "Long");


        when(userService.getCurrentUserLogin()).thenReturn(user);
        when(classRegistrationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        Page<?> result = studentService.search(params,Pageable.unpaged());

        assertNotNull(result);
        StudentDto studentDto = (StudentDto) result.getContent().get(0);
        assertEquals(studentDto.getEmail(), "Hoang@gmail.com");

    }

    @Test
    public void shouldNotReturnDataWhenSearch() {
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        User user = new User();
        user.setRole(role);

        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setClassName("cntt");
        List<Classroom> classrooms = new ArrayList<>();
        classrooms.add(classroom);
        user.setClassrooms(classrooms);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        when(classRegistrationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        Page<?> result = studentService.search(Collections.emptyMap(),Pageable.unpaged());

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

    }

}

