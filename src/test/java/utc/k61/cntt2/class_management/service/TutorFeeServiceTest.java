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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TutorFeeServiceTest {
    private TutorFeeRepository tutorFeeRepository;
    private ClassroomService classroomService;
    private UserService userService;

    private TutorFeeService tutorFeeService;

    @Before
    public void setUp() {
        tutorFeeRepository = mock(TutorFeeRepository.class);
        TutorFeeDetailRepository tutorFeeDetailRepository = mock(TutorFeeDetailRepository.class);
        classroomService = mock(ClassroomService.class);
        userService = mock(UserService.class);
        EmailService emailService = mock(EmailService.class);
        String tempFolder = "/temp";

        tutorFeeService = new TutorFeeService(tutorFeeRepository,
                tutorFeeDetailRepository,
                classroomService,
                userService,
                emailService,
                tempFolder);
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
    public void shouldThrowExceptionWhenGetTutorFeeForStudentNotFoundInfo(){
        Classroom classroom = new Classroom();
        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classroom.setClassRegistrations(classRegistrations);

        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        when(classroomService.getById(2L)).thenReturn(classroom);

        tutorFeeService.getTutorFeeForStudent(2L);

    }

    @Test
    public void shouldReturnDataWhenGetTutorFeeForStudentWithRoleStudent() {
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);

        Classroom classroom = new Classroom();
        ClassRegistration classRegistration = getClassRegistration(user);


        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classRegistrations.add(classRegistration);

        classroom.setClassRegistrations(classRegistrations);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        when(classroomService.getById(1L)).thenReturn(classroom);


        List<TutorFeeDetailDto> result = tutorFeeService.getTutorFeeForStudent(1L);
        assertEquals(result.size(), 1);
        TutorFeeDetailDto dtoExtracted = result.get(0);
        assertEquals(dtoExtracted.getStudentName(), "Huan Anh Nguyen");
        assertEquals(dtoExtracted.getLastName(), "Nguyen");
        //...

    }

    private static ClassRegistration getClassRegistration(User user) {
        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setStudent(user);
        TutorFeeDetail tutorFeeDetail = new TutorFeeDetail();
        tutorFeeDetail.setFeeAmount(100L);
        tutorFeeDetail.setFeeSubmitted(100L);
        tutorFeeDetail.setNumberOfAttendedLesson(1);

        //student
        tutorFeeDetail.setClassRegistration(classRegistration);
        //...
        // tutorfee
        TutorFee tutorFee = new TutorFee();
        tutorFee.setLessonPrice(100);
        tutorFeeDetail.setTutorFee(tutorFee);


        classRegistration.setTutorFeeDetails(List.of(tutorFeeDetail));
        classRegistration.setFirstName("Huan");
        classRegistration.setLastName("Nguyen");
        classRegistration.setSurname("Anh");

        classRegistration.setEmail("huan@gmail.com");
        classRegistration.setPhone("0364811562");
        return classRegistration;
    }


    @Test(expected = BusinessException.class)
    public void shouldThrowExceptionWhenPayWithNotHavePermission(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        tutorFeeService.pay(1L);
    }


    @Test(expected = ResourceNotFoundException.class)
    public void shouldThowExceptionWhenPayWithNotFoundInformation(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        tutorFeeService.pay(1L);
    }




    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowExceptionWhenRecalculateFeeWithNotFoundTutorFeeWithId(){
        tutorFeeService.reCalculateFee(1L, 1);
    }

    @Test
    public void shouldReturnDataWhenReCalculateFee(){
        TutorFee tutorFee = new TutorFee();
        tutorFee.setId(1L);
        tutorFee.setLessonPrice(100);
        tutorFee.setYear(2022);
        tutorFee.setMonth(2);

        Classroom classroom = new Classroom();
        tutorFee.setClassroom(classroom);

        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setDay(LocalDate.parse("2022-02-03"));
        List<ClassSchedule> classSchedules = new ArrayList<>();
        classSchedules.add(classSchedule);


        classroom.setSchedules(classSchedules);

        ClassRegistration classRegistration = new ClassRegistration();
        List<ClassRegistration> classRegistrations = new ArrayList<>();
        classRegistrations.add(classRegistration);
        classroom.setClassRegistrations(classRegistrations);


        when(tutorFeeRepository.findById(1L)).thenReturn(Optional.of(tutorFee));

        TutorFeeDetail tutorFeeDetail = new TutorFeeDetail();
        List<TutorFeeDetail> tutorFeeDetails = new ArrayList<>();
        tutorFeeDetails.add(tutorFeeDetail);
        tutorFee.setTutorFeeDetails(tutorFeeDetails);

        ClassAttendance classAttendance = new ClassAttendance();
        List<ClassAttendance> classAttendances = new ArrayList<>();
        classAttendances.add(classAttendance);
        classSchedule.setClassAttendances(classAttendances);


        classAttendance.setClassRegistration(classRegistration);
        classAttendance.setIsAttended(true);


        tutorFeeService.reCalculateFee(1L, 100);
    }

}

