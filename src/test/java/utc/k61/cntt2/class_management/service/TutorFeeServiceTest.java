package utc.k61.cntt2.class_management.service;

import org.junit.Before;
import org.junit.Test;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.TutorFeeDetailDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.TutorFeeDetailRepository;
import utc.k61.cntt2.class_management.repository.TutorFeeRepository;
import utc.k61.cntt2.class_management.service.email.EmailService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TutorFeeServiceTest {
    private TutorFeeRepository tutorFeeRepository;
    protected TutorFeeDetailRepository tutorFeeDetailRepository;
    private ClassroomService classroomService;
    private UserService userService;
    protected EmailService emailService;
    public String tempFolder;

    private TutorFeeService tutorFeeService;

    @Before
    public void setUp() {
        tutorFeeRepository = mock(TutorFeeRepository.class);
        tutorFeeDetailRepository = mock(TutorFeeDetailRepository.class);
        classroomService = mock(ClassroomService.class);
        userService = mock(UserService.class);
        emailService = mock(EmailService.class);
        tempFolder = "/temp";

        tutorFeeService = new TutorFeeService(tutorFeeRepository, tutorFeeDetailRepository, classroomService, userService, emailService, tempFolder);

    }


    @Test(expected = BusinessException.class)
    public void shouldThrowExceptionWhenGetTutorFeeForStudentWithUserRoleIsNotStudent() {
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        tutorFeeService.getTutorFeeForStudent(1L);
    }
    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundExceptionWhenGetTutorFeeForStudentWithUserRoleIsNotStudent(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);

        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setStudent(user);
        ClassRegistration classRegistration1 = new ClassRegistration();
        ClassRegistration classRegistration2 = new ClassRegistration();

        List<ClassRegistration> classRegistrations = new ArrayList<>();
        //classRegistrations.add(classRegistration);
        classRegistrations.add(classRegistration1);
        classRegistrations.add(classRegistration2);

        Classroom classroom = new Classroom();
        classroom.setClassRegistrations(classRegistrations);

        when(classroomService.getById(2L)).thenReturn(classroom);

        tutorFeeService.getTutorFeeForStudent(2L);
    }

    @Test
    public void shouldReturnDataWhenGetTutorFeeForStudentWithRoleStudent() {
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);
        when(userService.getCurrentUserLogin()).thenReturn(user);

        Classroom classroom = new Classroom();
        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setStudent(user);
        classRegistration.setFirstName("Nguyen");
        classRegistration.setLastName("Hieu");
        classRegistration.setEmail("hieu@gmail.com");
        classRegistration.setPhone("0856495381");


        List<TutorFeeDetail> tutorFeeDetails = new ArrayList<>();
        //build TutorFeeDto
        TutorFeeDetail tutorFeeDetail = new TutorFeeDetail();
        tutorFeeDetail.setNumberOfAttendedLesson(3);
        tutorFeeDetail.setFeeSubmitted(1L);
        tutorFeeDetail.setId(5L);
        TutorFee tutorFee = new TutorFee();
        tutorFee.setTotalLesson(3);
        tutorFee.setLessonPrice(2);
        tutorFeeDetail.setTutorFee(tutorFee);
        tutorFeeDetail.setClassRegistration(classRegistration);
        tutorFeeDetails.add(tutorFeeDetail);



        classRegistration.setTutorFeeDetails(tutorFeeDetails);

        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classRegistrations.add(classRegistration);

        classroom.setClassRegistrations(classRegistrations);

        when(classroomService.getById(1L)).thenReturn(classroom);

        List<TutorFeeDetailDto> result = (List<TutorFeeDetailDto>) tutorFeeService.getTutorFeeForStudent(1L);
        assertEquals(result.size(),1);
        TutorFeeDetailDto tutorFeeDetailDto = result.get(0);
        assertEquals(tutorFeeDetailDto.getLastName(),"Hieu");

    }

    @Test(expected = ResourceNotFoundException.class)
    public void returnResourceNotFoundExceptionWhenReCalculateFee(){
        Long tutorFeeId = 1L;
        TutorFee tutorFee = new TutorFee();
//        tutorFee.setLessonPrice(classSessionPrice);
        when(tutorFeeRepository.findById(tutorFeeId)).thenReturn(Optional.empty());

        assertEquals(tutorFeeService.reCalculateFee(1L,1),tutorFee);
    }

    @Test
    public void returnDataWhenReCalculateFee() {
        ClassSchedule classSchedule = new ClassSchedule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        LocalDate localDate = LocalDate.parse("2023/02/16", formatter);
        classSchedule.setDay(localDate);
        List<ClassSchedule> classSchedules = new ArrayList<>();

        Classroom classroom = new Classroom();

        classroom.setSchedules(classSchedules);

        Long tutorFeeId = 1L;
        int classSessionPrice = 1;
        TutorFee tutorFee = new TutorFee();
        tutorFee.setLessonPrice(classSessionPrice);
        tutorFee.setClassroom(classroom);
        tutorFee.setMonth(2);
        tutorFee.setYear(2023);
        when(tutorFeeRepository.findById(tutorFeeId)).thenReturn(Optional.of(tutorFee));


    }
}