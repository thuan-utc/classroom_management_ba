package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassSchedule;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.NewClassScheduleRequest;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ClassScheduleRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassroomRepository classroomRepository;

    @Autowired
    public ClassScheduleService(ClassScheduleRepository classScheduleRepository,
                                ClassroomRepository classroomRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.classroomRepository = classroomRepository;
    }

    public List<ClassSchedule> getAllClassSchedule(Long classId) {
        return classScheduleRepository.findAllByClassroomIdOrderByDayAsc(classId);
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
        classScheduleRepository.saveAll(classSchedules);
        log.info("Created class schedule for class {}", classroom.getClassName());
        return new ApiResponse(true, "success");
    }
}
