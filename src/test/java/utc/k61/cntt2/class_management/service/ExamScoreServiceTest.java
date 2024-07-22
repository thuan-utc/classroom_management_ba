package utc.k61.cntt2.class_management.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.ExamScoreDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.ExamRepository;
import utc.k61.cntt2.class_management.repository.ExamScoreRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamScoreServiceTest {
    private ExamScoreRepository  examScoreRepository;
    private UserService userService;
    private ExamScoreService examScoreService;

    @Before
    public void setUp() {
        examScoreRepository = mock(ExamScoreRepository.class);
        ExamRepository examRepository = mock(ExamRepository.class);
        userService = mock(UserService.class);
        ClassroomService classroomService = mock(ClassroomService.class);
        String tempFolder = "/temp";

        examScoreService = new ExamScoreService( examScoreRepository, examRepository, userService, classroomService, tempFolder);
    }

    @Test(expected = BusinessException.class)
    public  void shouldThrowExceptionWhenGetExamScoreWithRequireRoleStudent() {
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.STUDENT);
        user.setRole(role);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        examScoreService.getExamScore(1L);
    }

    @Test
    public void shouldReturnDataWhenGetExamScore(){
        User user = new User();
        Role role = new Role();
        role.setName(RoleName.TEACHER);
        user.setRole(role);

        Exam exam = new Exam();
        exam.setName("Final Test");
        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setId(1L);

        ExamScore examScore = new ExamScore();
        examScore.setExam(exam);
        examScore.setClassRegistration(classRegistration);

        List<ExamScore> examScores = new ArrayList<>();
        examScores.add(examScore);

        when(userService.getCurrentUserLogin()).thenReturn(user);
        when(examScoreRepository.findAllByClassRegistrationId(1L)).thenReturn(examScores);


        Page<?> page = (Page<?>) examScoreService.getExamScore(1L);
        assertNotNull(page);

        // Cast the Object to the correct type

        ExamScoreDto examScoreDto = (ExamScoreDto) page.getContent().get(0);
        // Now you can access the methods of ExamScoreDto
        String examName = examScoreDto.getExamName();
        assertEquals("Final Test", examName);

    }

}