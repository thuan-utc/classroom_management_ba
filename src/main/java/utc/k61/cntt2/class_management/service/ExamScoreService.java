package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.ExamScoreDto;
import utc.k61.cntt2.class_management.dto.NewExamRequest;
import utc.k61.cntt2.class_management.dto.StudentExamResultDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ExamRepository;
import utc.k61.cntt2.class_management.repository.ExamScoreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ExamScoreService {
    private final ExamScoreRepository examScoreRepository;
    private final ExamRepository examRepository;
    private final UserService userService;
    private final ClassroomService classroomService;

    @Autowired
    public ExamScoreService(
            ExamScoreRepository examScoreRepository,
            ExamRepository examRepository,
            UserService userService,
            ClassroomService classroomService) {
        this.examScoreRepository = examScoreRepository;
        this.examRepository = examRepository;
        this.userService = userService;
        this.classroomService = classroomService;
    }

    public ApiResponse createNewExam(NewExamRequest request) {
        User user = userService.getCurrentUserLogin();
        if (user.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Require role teacher!");
        }
        Classroom classroom = classroomService.getById(request.getClassId());
        Exam exam = new Exam();
        exam.setName(request.getExamName());
        exam.setClassroom(classroom);
        examRepository.save(exam);

        return new ApiResponse(true, "Success");
    }

    public Page<?> fetchAllExamScore(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found exam"));
        Classroom classroom = exam.getClassroom();
        List<ClassRegistration> classRegistrations = classroom.getClassRegistrations();
        List<ExamScore> examScoreListExist = exam.getExamScoreList();
        List<ExamScore> examScoreList = new ArrayList<>();
        for (ClassRegistration classRegistration : classRegistrations) {
            Optional<ExamScore> existOne = examScoreListExist.stream()
                    .filter(exitOne -> StringUtils.equalsIgnoreCase(exitOne.getClassRegistration().getEmail(),
                            classRegistration.getEmail()))
                    .findAny();
            if (existOne.isEmpty()) {
                ExamScore examScore = new ExamScore();
                examScore.setClassRegistration(classRegistration);
                examScore.setExam(exam);

                examScoreList.add(examScore);
            }
        }
        examScoreList.addAll(examScoreListExist);
        examScoreList = examScoreRepository.saveAll(examScoreList);

        List<ExamScoreDto> examScoreDtos = new ArrayList<>();
        for (ExamScore examScore : examScoreList) {
            ExamScoreDto examScoreDto = new ExamScoreDto();
            examScoreDto.setId(examScore.getId());
            ClassRegistration student = examScore.getClassRegistration();
            examScoreDto.setName(student.getFirstName() + " " + student.getSurname() + " " + student.getLastName());
            examScoreDto.setEmail(student.getEmail());
            examScoreDto.setScore(examScore.getScore());

            examScoreDtos.add(examScoreDto);
        }

        return new PageImpl<>(examScoreDtos);
    }

    public Object saveExamResult(List<ExamScoreDto> examScoreDtos) {
        List<Long> examScoreIds = examScoreDtos.stream().map(ExamScoreDto::getId).collect(Collectors.toList());
        List<ExamScore> examScoreList = examScoreRepository.findAllByIdIn(examScoreIds);
        for (ExamScoreDto examScoreDto : examScoreDtos) {
            if (examScoreDto.getScore() != null) {
                Optional<ExamScore> examScore = examScoreList.stream().filter(a -> examScoreDto.getId() == a.getId()).findFirst();
                examScore.ifPresent(classAttendance -> classAttendance.setScore(examScoreDto.getScore()));
            }
        }

        examScoreRepository.saveAll(examScoreList);
        return new ApiResponse(true, "Success");
    }

    public Page<?> fetchAllExam(Long classId) {
        Classroom classroom = classroomService.getById(classId);
        return new PageImpl<>(classroom.getExams());
    }

    public Object getStudentExamResult(Long classId) {
        User user = userService.getCurrentUserLogin();
        Classroom classroom = classroomService.getById(classId);
        List<Exam> exams = classroom.getExams();
        List<ExamScore> currentUserResult = new ArrayList<>();
        for (Exam exam : exams) {
            List<ExamScore> examScoreList = exam.getExamScoreList();
            for (ExamScore examScore : examScoreList) {
                if (examScore.getClassRegistration() != null
                        && StringUtils.equalsIgnoreCase(examScore.getClassRegistration().getEmail(), user.getEmail())) {
                    currentUserResult.add(examScore);
                    break;
                }
            }
        }

        List<StudentExamResultDto> resultList = new ArrayList<>();
        for (ExamScore examScore : currentUserResult) {
            StudentExamResultDto resultDto = new StudentExamResultDto();
            resultDto.setExamName(examScore.getExam().getName());
            resultDto.setScore(examScore.getScore());

            resultList.add(resultDto);
        }

        return new PageImpl<>(resultList);
    }
}

