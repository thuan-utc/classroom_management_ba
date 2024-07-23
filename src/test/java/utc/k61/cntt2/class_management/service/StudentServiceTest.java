package utc.k61.cntt2.class_management.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.StudentDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.ClassAttendanceRepository;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import java.util.*;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudentServiceTest {
    private ClassRegistrationRepository classRegistrationRepository;
    private UserService userService;
    private ClassroomService classroomService;
    private StudentService studentService;

    private ClassroomRepository classroomRepository;

    @Before
    public void setUp() {
        classRegistrationRepository = mock(ClassRegistrationRepository.class);
        mock(ClassAttendanceRepository.class);
        classroomRepository = mock(ClassroomRepository.class);
        userService = mock(UserService.class);
        classroomService = mock(ClassroomService.class);
        String tempFolder = "/temp";
        
        studentService = new StudentServiceBuilder().setClassRegistrationRepository(classRegistrationRepository).setClassroomRepository(classroomRepository).setUserService(userService).setClassroomService(classroomService).setTempFolder(tempFolder).createStudentService();
    }

    @Test
    public void testAddStudentForClassWhenUserExisting() {
        Classroom classroom = new Classroom();
        when(classroomService.getById(1L)).thenReturn(classroom);

        StudentDto studentDto = new StudentDto();
        studentDto.setFirstName("Nguyen");
        studentDto.setSurname("Anh");
        studentDto.setLastName("Hieu");
        studentDto.setEmail("hieu@gmail.com");
        studentDto.setPhone("0866437555");
        studentDto.setAddress("Ha Noi");
        
        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setClassroom(classroom);
        classRegistration.setEmail(studentDto.getEmail());


        List<User> users = new ArrayList<>();
        when(userService.findAllByEmailIn(List.of(studentDto.getEmail()))).thenReturn(users);

        ApiResponse response = (ApiResponse) studentService.addStudentForClass(studentDto, 1L);

        assertEquals(true, response.getSuccess());
        assertEquals("Success", response.getMessage());

    }

    @Test
    public void returnExceptionWhenAddStudentForClass() {
        Classroom classroom = new Classroom();
        when(classroomService.getById(1L)).thenReturn(classroom);

        StudentDto studentDto = new StudentDto();
        studentDto.setFirstName("Nguyen");
        studentDto.setSurname("Anh");
        studentDto.setLastName("Hieu");
        studentDto.setEmail("hieu@gmail.com");
        studentDto.setPhone("0866437555");
        studentDto.setAddress("Ha Noi");

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setClassroom(classroom);
        classRegistration.setEmail(studentDto.getEmail());

        when(userService.findAllByEmailIn(List.of(studentDto.getEmail()))).thenReturn(emptyList());

        ApiResponse response = (ApiResponse) studentService.addStudentForClass(studentDto, 1L);

        assertEquals(true, response.getSuccess());
        assertEquals("Success", response.getMessage());
    }

    // test for student detail
    @Test(expected = IllegalArgumentException.class)
    public void testGetStudentDeteilReturnIllegalArgumentException() {
        long studentId = 1L;
        when(classRegistrationRepository.findById(studentId)).thenReturn(Optional.empty());

        studentService.getStudentDetail(studentId);
    }

    @Test
    public void testGetStudentDetailReturnData(){
        long studentId = 1L;
        List<TutorFeeDetail> tutorFeeDetails = new ArrayList<>();

        ClassRegistration classRegistration = getClassRegistration(studentId, tutorFeeDetails);

        when(classRegistrationRepository.findById(studentId)).thenReturn(Optional.of(classRegistration));

        StudentDto studentDto = studentService.getStudentDetail(studentId);

        assertNotNull(studentDto);
        assertEquals(studentDto.getLastName(),classRegistration.getLastName());
    }

    private static ClassRegistration getClassRegistration(long studentId, List<TutorFeeDetail> tutorFeeDetails) {
        Classroom classroom = new Classroom();
        classroom.setClassName("JAVA");

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setId(studentId);
        classRegistration.setTutorFeeDetails(tutorFeeDetails);
        classRegistration.setFirstName("Nguyễn");
        classRegistration.setSurname("Anh");
        classRegistration.setLastName("Hiếu");
        classRegistration.setEmail("hieu@gmail.com");
        classRegistration.setPhone("0378588468");
        classRegistration.setClassroom(classroom);
        return classRegistration;
    }
    @Test(expected = BusinessException.class)
    public void testGetAllStudentReturnBusinessException(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);
        when(userService.getCurrentUserLogin()).thenReturn(user);

        Map<String, String> params = new HashMap<>();
        studentService.getAllStudent(params, null);
    }

    @Test
    public void testGetAllStudentReturnData() {
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        user.setRole(role);
        user.setId(1L);

        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setClassName("java");
        List<Classroom> classrooms;
        classrooms = new ArrayList<>();
        classrooms.add(classroom);
        when(classroomRepository.findAllByTeacherId(user.getId())).thenReturn(classrooms);

        Map<String, String> params = new HashMap<>();
        params.put("firstName","Nguyễn");
//        params.put("surName","Anh");
//        params.put("LastName","Hiếu");
//        params.put("email","hieu@gmail.com");
//        params.put("phone","0388568459");
        params.put("className",classroom.getClassName());

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setFirstName("Nguyễn");
        classRegistration.setClassroom(classroom);
        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classRegistrations.add(classRegistration);

        Page<ClassRegistration> all = new PageImpl<>(classRegistrations);

        when(classRegistrationRepository.findAll(Specification.where(null),Pageable.unpaged())).thenReturn(all);

        assertEquals(params.get("firstName"),classRegistrations.get(0).getFirstName());

    }
}