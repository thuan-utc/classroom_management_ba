package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.ClassAttendanceDto;
import utc.k61.cntt2.class_management.dto.StudentAttendanceResultDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ClassAttendanceRepository;
import utc.k61.cntt2.class_management.repository.ClassScheduleRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ClassAttendanceService {
    private final ClassAttendanceRepository classAttendanceRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;

    @Autowired
    public ClassAttendanceService(
            ClassAttendanceRepository classAttendanceRepository,
            ClassScheduleRepository classScheduleRepository,
            ClassroomRepository classroomRepository,
            UserService userService) {
        this.classAttendanceRepository = classAttendanceRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classroomRepository = classroomRepository;
        this.userService = userService;
    }

    public Page<?> fetchClassAttendance(Long scheduleId) {
        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found class schedule"));
        List<ClassAttendance> exisits = classAttendanceRepository.findAllByClassScheduleId(scheduleId);
        Classroom classroom = schedule.getClassroom();
        List<ClassRegistration> classRegistrations = classroom.getClassRegistrations();
        List<ClassAttendance> classAttendances = new ArrayList<>();
        for (ClassRegistration classRegistration : classRegistrations) {
            Optional<ClassAttendance> existOne = exisits.stream()
                    .filter(exitOne -> StringUtils.equalsIgnoreCase(exitOne.getClassRegistration().getEmail(),
                            classRegistration.getEmail()))
                    .findAny();
            if (existOne.isEmpty()) {
                ClassAttendance classAttendance = new ClassAttendance();
                classAttendance.setClassRegistration(classRegistration);
                classAttendance.setClassSchedule(schedule);
                classAttendance.setIsAttended(false);

                classAttendances.add(classAttendance);
            }
        }

        classAttendances.addAll(exisits);
        classAttendances = classAttendanceRepository.saveAll(classAttendances);
        List<ClassAttendanceDto> result = new ArrayList<>();
        for (ClassAttendance classAttendance : classAttendances) {
            ClassAttendanceDto classAttendanceDto = new ClassAttendanceDto();
            classAttendanceDto.setId(classAttendance.getId());
            ClassRegistration student = classAttendance.getClassRegistration();
            classAttendanceDto.setName(student.getFirstName() + " " + student.getSurname() + " " + student.getLastName());
            classAttendanceDto.setIsAttended(classAttendance.getIsAttended());
            classAttendanceDto.setEmail(classAttendance.getClassRegistration().getEmail());

            result.add(classAttendanceDto);
        }
        return new PageImpl<>(result);
    }

    public Page<?> saveAttendanceResult(List<ClassAttendanceDto> attendanceResults) {
        List<Long> attendanceIds = attendanceResults.stream().map(ClassAttendanceDto::getId).collect(Collectors.toList());
        List<ClassAttendance> attendances = classAttendanceRepository.findAllByIdIn(attendanceIds);
        for (ClassAttendanceDto attendanceDto : attendanceResults) {
            if (attendanceDto.getIsAttended() != null) {
                Optional<ClassAttendance> attendance = attendances.stream().filter(a -> attendanceDto.getId().equals(a.getId())).findFirst();
                attendance.ifPresent(classAttendance -> classAttendance.setIsAttended(attendanceDto.getIsAttended()));
            }
        }

        classAttendanceRepository.saveAll(attendances);
        return new PageImpl<>(attendances);
    }

    public Object getStudentAttendanceResult(Long classId) {
        User currentLoginUser = userService.getCurrentUserLogin();
        if (currentLoginUser.getRole().getName() != RoleName.STUDENT) {
            throw new BusinessException("Require Role Student!");
        }
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found classroom"));
        List<ClassSchedule> classSchedules = classroom.getSchedules();
        List<ClassAttendance> currentStudentAttendanceResult = new ArrayList<>();
        for (ClassSchedule classSchedule : classSchedules) {
            List<ClassAttendance> attendanceResults = classSchedule.getClassAttendances();
            for (ClassAttendance attendance : attendanceResults) {
                if (attendance.getClassRegistration() != null
                        && StringUtils.equalsIgnoreCase(attendance.getClassRegistration().getEmail(), currentLoginUser.getEmail())) {
                    currentStudentAttendanceResult.add(attendance);
                    break;
                }
            }
        }

        List<StudentAttendanceResultDto> resultList = new ArrayList<>();
        for (ClassAttendance attendance : currentStudentAttendanceResult) {
            StudentAttendanceResultDto resultDto = new StudentAttendanceResultDto();
            resultDto.setDay(attendance.getClassSchedule().getDay());
            resultDto.setClassPeriod(attendance.getClassSchedule().getPeriodInDay());
            resultDto.setAttended(attendance.getIsAttended());

            resultList.add(resultDto);
        }

        return new PageImpl<>(resultList);
    }
}
