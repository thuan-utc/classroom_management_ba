package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassSchedule;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.NewClassScheduleRequest;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ClassAttendanceRepository;
import utc.k61.cntt2.class_management.repository.ClassScheduleRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassroomRepository classroomRepository;
    private final TutorFeeService tutorFeeService;
    private final UserService userService;
    private final ClassAttendanceRepository classAttendanceRepository;

    @Autowired
    public ClassScheduleService(
            ClassScheduleRepository classScheduleRepository,
            ClassroomRepository classroomRepository,
            TutorFeeService tutorFeeService,
            UserService userService, ClassAttendanceRepository classAttendanceRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.classroomRepository = classroomRepository;
        this.tutorFeeService = tutorFeeService;
        this.userService = userService;
        this.classAttendanceRepository = classAttendanceRepository;
    }

    public List<ClassSchedule> getAllClassSchedule(Long classId) {
        return classScheduleRepository.findAllByClassroomIdOrderByDayAsc(classId);
    }

    public Page<ClassSchedule> search(Map<String, String> params, Pageable pageable) {
        Specification<ClassSchedule> specs = getSpecification(params);
        return classScheduleRepository.findAll(specs, pageable);
    }

    private Specification<ClassSchedule> getSpecification(Map<String, String> params) {
        return Specification.where((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = null;
            List<Predicate> predicateList = new ArrayList<>();
            for (Map.Entry<String, String> p : params.entrySet()) {
                String key = p.getKey();
                String value = p.getValue();
                if (!"page".equalsIgnoreCase(key) && !"size".equalsIgnoreCase(key) && !"sort".equalsIgnoreCase(key)) {
                    if (StringUtils.equalsIgnoreCase("startCreatedDate", key)) { //"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                        predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toInstant(ZoneOffset.UTC)));
                    } else if (StringUtils.equalsIgnoreCase("endCreatedDate", key)) {
                        predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toInstant(ZoneOffset.UTC)));
                    } else if (StringUtils.equalsIgnoreCase("classId", key)) {
                        predicateList.add(criteriaBuilder.equal(root.get("classroom").get("id"), Long.valueOf(value)));
                    } else {
                        if (value != null && (value.contains("*") || value.contains("%"))) {
                            predicateList.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                        } else if (value != null) {
                            predicateList.add(criteriaBuilder.like(root.get(key), value + "%"));
                        }
                    }
                }
            }

            if (!predicateList.isEmpty()) {
                predicate = criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }

            return predicate;
        });
    }


    public ApiResponse createClassSchedule(NewClassScheduleRequest request) {
        Classroom classroom = classroomRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found classroom"));
        List<ClassSchedule> classSchedules = new ArrayList<>();
        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            // Check if the current date matches the specified day of the week
            if (date.getDayOfWeek().name().equals(request.getDayInWeek())) {
                // Create a new ClassSchedule object for the current date
                ClassSchedule classSchedule = new ClassSchedule();
                classSchedule.setClassroom(classroom);
                classSchedule.setDay(date);
                classSchedule.setPeriodInDay(request.getPeriodInDay());
                classSchedule.setDayInWeek(request.getDayInWeek());

                // Add the created ClassSchedule to the list
                classSchedules.add(classSchedule);
            }
        }
        User user = userService.getCurrentUserLogin();
        List<Classroom> classrooms = user.getClassrooms();
        List<ClassSchedule> existingSchedule = classrooms.stream()
                .flatMap(classroom1 -> classroom1.getSchedules().stream())
                .collect(Collectors.toList());
        for (ClassSchedule classSchedule : classSchedules) {
            Optional<ClassSchedule> conflictSchedule = existingSchedule.stream()
                    .filter(existOne -> existOne.getDay().equals(classSchedule.getDay())
                    && existOne.getPeriodInDay().equals(classSchedule.getPeriodInDay()))
                    .findFirst();
            if (conflictSchedule.isPresent()) {
                String message = "Trùng lịch: "
                        + conflictSchedule.get().getClassroom().getClassName()
                        + " - " + classSchedule.getDay()
                        + " - " + classSchedule.getPeriodInDay().getName();
                throw new BusinessException(message);
            }
        }
        classScheduleRepository.saveAll(classSchedules);
        log.info("Created class schedule for class {}", classroom.getClassName());
        return new ApiResponse(true, "success");
    }

    @Transactional
    public ApiResponse deleteSchedule(Long scheduleId) {
        User user = userService.getCurrentUserLogin();
        if (user.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Missing permission");
        }
        List<Classroom> classrooms = user.getClassrooms();
        List<ClassSchedule> schedules = classrooms.stream().flatMap(classroom -> classroom.getSchedules().stream()).collect(Collectors.toList());
        schedules.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found schedule"));
        try {
            classAttendanceRepository.deleteAllByClassScheduleId(scheduleId);
            classScheduleRepository.deleteById(scheduleId);
        } catch (Exception e) {
            log.error("Exception during delete operation", e);
            throw new BusinessException("Deletion failed due to an error");
        }

        return new ApiResponse(true,"Success");
    }
}
