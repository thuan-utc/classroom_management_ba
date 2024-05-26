package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.ClassSchedule;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.ClassPeriodInWeek;
import utc.k61.cntt2.class_management.dto.DashboardDataDto;
import utc.k61.cntt2.class_management.enumeration.ClassPeriod;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;
import utc.k61.cntt2.class_management.repository.ClassScheduleRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DashboardService {

    private final UserService userService;
    private final ClassroomRepository classroomRepository;
    private final ClassRegistrationRepository classRegistrationRepository;
    private final ClassScheduleRepository classScheduleRepository;

    @Autowired
    public DashboardService(
            UserService userService,
            ClassroomRepository classroomRepository,
            ClassRegistrationRepository classRegistrationRepository,
            ClassScheduleRepository classScheduleRepository) {
        this.userService = userService;
        this.classroomRepository = classroomRepository;
        this.classRegistrationRepository = classRegistrationRepository;
        this.classScheduleRepository = classScheduleRepository;
    }

    public List<ClassPeriodInWeek> fetchClassInCurrentWeek() {
        List<ClassPeriodInWeek> classPeriodInWeeks = new ArrayList<>();
        User userCurrent = userService.getCurrentUserLogin();
        LocalDate currentDate = LocalDate.now();
        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Classroom> classrooms;
        if (userCurrent.getRole().getName() == RoleName.TEACHER) {
            classrooms = classroomRepository.findAllByTeacherId(userCurrent.getId());
        } else if (userCurrent.getRole().getName() == RoleName.STUDENT) {
            List<ClassRegistration> classRegistrations = classRegistrationRepository.findAllByStudentId(userCurrent.getId());
            classrooms = classRegistrations.stream().map(ClassRegistration::getClassroom).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
        List<Long> classIds = classrooms.stream().map(Classroom::getId).collect(Collectors.toList());
        List<ClassSchedule> classSchedules = classScheduleRepository.findAllByClassroomIdInAndDayBetween(classIds, startOfWeek, endOfWeek);
        Map<ClassPeriod, List<ClassSchedule>> classPeriodListMap = classSchedules.stream().collect(Collectors.groupingBy(ClassSchedule::getPeriodInDay));
        for (ClassPeriod period : ClassPeriod.values()) {
            ClassPeriodInWeek classPeriodInWeek = new ClassPeriodInWeek();
            classPeriodInWeek.setClassPeriod(period);
            if (classPeriodListMap.containsKey(period)) {
                for (ClassSchedule classSchedule : classPeriodListMap.get(period)) {
                    switch (classSchedule.getDayInWeek()) {
                        case "MONDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getMondayClass())) {
                                classPeriodInWeek.setMondayClass(classPeriodInWeek.getMondayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setMondayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;
                        case "TUESDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getTuesdayClass())) {
                                classPeriodInWeek.setTuesdayClass(classPeriodInWeek.getTuesdayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setTuesdayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;
                        case "WEDNESDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getWednesdayClass())) {
                                classPeriodInWeek.setWednesdayClass(classPeriodInWeek.getWednesdayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setWednesdayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;
                        case "THURSDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getThursdayClass())) {
                                classPeriodInWeek.setThursdayClass(classPeriodInWeek.getThursdayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setThursdayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;
                        case "FRIDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getFridayClass())) {
                                classPeriodInWeek.setFridayClass(classPeriodInWeek.getFridayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setFridayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;
                        case "SATURDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getSaturdayClass())) {
                                classPeriodInWeek.setSaturdayClass(classPeriodInWeek.getSaturdayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setSaturdayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;
                        case "SUNDAY":
                            if (StringUtils.isNotBlank(classPeriodInWeek.getSundayClass())) {
                                classPeriodInWeek.setSundayClass(classPeriodInWeek.getSundayClass() + ";" + classSchedule.getClassroom().getClassName());
                            } else {
                                classPeriodInWeek.setSundayClass(classSchedule.getClassroom().getClassName());
                            }
                            break;

                    }
                }
            }

            classPeriodInWeeks.add(classPeriodInWeek);
        }

        return classPeriodInWeeks;
    }

    public DashboardDataDto getDashboardData() {
        User user = userService.getCurrentUserLogin();
        DashboardDataDto dashboardDataDto = new DashboardDataDto();
        if (user.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Require role Teacher");
        }
        List<Classroom> classrooms = user.getClassrooms();
        List<Classroom> currentClassrooms = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            List<ClassSchedule> classSchedules = classroom.getSchedules();
            for (ClassSchedule classSchedule : classSchedules) {
                if (classSchedule.getDay().isAfter(LocalDate.now())) {
                    currentClassrooms.add(classroom);
                    break;
                }
            }
        }

        dashboardDataDto.setNumberOfClass(currentClassrooms.size());
        int numberOfStudent = 0;
        for (Classroom classroom : classrooms) {
            numberOfStudent += classroom.getClassRegistrations().size();
        }
        dashboardDataDto.setNumberOfStudent(numberOfStudent);

        return dashboardDataDto;
    }

}
