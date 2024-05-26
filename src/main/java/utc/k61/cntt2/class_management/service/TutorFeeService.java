package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassAttendance;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.ClassSchedule;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.dto.TutorFeeDto;
//import utc.k61.cntt2.class_management.repository.TutorFeeRepository;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class TutorFeeService {
    //    private final TutorFeeRepository tutorFeeRepository;
    private final ClassroomService classroomService;

    @Autowired
    public TutorFeeService(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }


//    public Page<TutorFee> search(Map<String, String> params, Pageable pageable) {
//        Specification<TutorFee> specs = getSpecification(params);
//        if (tutorFeeRepository.findAll(specs).isEmpty()) {
//            String classId = params.get("classId");
//            if (StringUtils.isBlank(classId)) {
//                throw new BusinessException("Not found classroom");
//            }
//            refreshTutorFee(Long.parseLong(classId));
//        }
//        return tutorFeeRepository.findAll(specs, pageable);
//    }

//    public void refreshTutorFee(long classId) {
//        Classroom classroom = classroomService.getById(classId);
//        List<ClassSchedule> classSchedules = classroom.getSchedules();
//        if (classSchedules.isEmpty()) {
//            return;
//        }
//        Map<String, List<ClassSchedule>> groupByMonthYears = groupByMonthYear(classSchedules);
//        List<TutorFee> tutorFees = new ArrayList<>();
//        groupByMonthYears.forEach((monthYear, classSchedulesList) -> {
//            String[] monthAndYear = monthYear.split("-");
//            int month = Integer.parseInt(monthAndYear[0]);
//            int year = Integer.parseInt(monthAndYear[1]);
//            TutorFee tutorFee = new TutorFee();
//            tutorFee.setYear(year);
//            tutorFee.setMonth(month);
//            tutorFee.setTotalNumberOfClasses(classSchedulesList.size());
//            tutorFee.setClassroom(classroom);
//
//            tutorFees.add(tutorFee);
//        });
//        tutorFeeRepository.deleteAllByClassroomId(classroom.getId());
//        tutorFeeRepository.saveAll(tutorFees);
//    }

//    public Map<String, List<ClassSchedule>> groupByMonthYear(List<ClassSchedule> schedules) {
//        Map<String, List<ClassSchedule>> mapByMonthYear = new HashMap<>();
//
//        for (ClassSchedule schedule : schedules) {
//            LocalDate day = schedule.getDay();
//            int month = day.getMonthValue();
//            int year = day.getYear();
//            String monthYearKey = month + "-" + year; // Create a unique key
//
//            // Retrieve the list of schedules for the current month-year combination
//            List<ClassSchedule> schedulesForMonthYear = mapByMonthYear.getOrDefault(monthYearKey, new ArrayList<>());
//
//            // Add the current schedule to the list
//            schedulesForMonthYear.add(schedule);
//
//            // Update the map with the updated list
//            mapByMonthYear.put(monthYearKey, schedulesForMonthYear);
//        }
//
//        return mapByMonthYear;
//    }
//
//
//    private Specification<TutorFee> getSpecification(Map<String, String> params) {
//        return Specification.where((root, criteriaQuery, criteriaBuilder) -> {
//            Predicate predicate = null;
//            List<Predicate> predicateList = new ArrayList<>();
//            for (Map.Entry<String, String> p : params.entrySet()) {
//                String key = p.getKey();
//                String value = p.getValue();
//                if (!"page".equalsIgnoreCase(key) && !"size".equalsIgnoreCase(key) && !"sort".equalsIgnoreCase(key)) {
//                    if (StringUtils.equalsIgnoreCase("startCreatedDate", key)) { //"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
//                        predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toInstant(ZoneOffset.UTC)));
//                    } else if (StringUtils.equalsIgnoreCase("endCreatedDate", key)) {
//                        predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toInstant(ZoneOffset.UTC)));
//                    } else if (StringUtils.equalsIgnoreCase("classId", key)) {
//                        predicateList.add(criteriaBuilder.equal(root.get("classroom").get("id"), Long.valueOf(value)));
//                    } else {
//                        if (value != null && (value.contains("*") || value.contains("%"))) {
//                            predicateList.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
//                        } else if (value != null) {
//                            predicateList.add(criteriaBuilder.like(root.get(key), value + "%"));
//                        }
//                    }
//                }
//            }
//
//            if (!predicateList.isEmpty()) {
//                predicate = criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
//            }
//
//            return predicate;
//        });
//    }

    public Page<?> calculateFee(Long classId, Integer month, Integer year, Integer classSessionPrice) {
        Classroom classroom = classroomService.getById(classId);
        List<ClassSchedule> classSchedules = classroom.getSchedules();
        List<ClassSchedule> scheduleFiltered = classSchedules.stream()
                .filter(classSchedule -> classSchedule.getDay().getYear() == year
                        && classSchedule.getDay().getMonth().getValue() == month)
                .collect(Collectors.toList());
        List<ClassRegistration> classRegistrations = classroom.getClassRegistrations();
        List<TutorFeeDto> tutorFeeDtos = new ArrayList<>();
        Map<ClassRegistration, Integer> attendanceMap = new HashMap<>();
        for (ClassSchedule schedule : scheduleFiltered) {
            List<ClassAttendance> attendances = schedule.getClassAttendances();
            for (ClassAttendance attendance : attendances) {
                ClassRegistration registration = attendance.getClassRegistration();
                boolean isAttended = attendance.getIsAttended() != null && attendance.getIsAttended();
                attendanceMap.put(registration, attendanceMap.getOrDefault(registration, 0) + (isAttended ? 1 : 0));
            }
        }
        classRegistrations.sort(Comparator.comparing(ClassRegistration::getLastName));
        for (ClassRegistration student : classRegistrations) {
            TutorFeeDto tutorFeeDto = new TutorFeeDto();
            tutorFeeDto.setStudentName(student.getFirstName() + " " + student.getSurname() + " " + student.getLastName());
            tutorFeeDto.setEmail(student.getEmail());
            tutorFeeDto.setPhone(student.getPhone());
            tutorFeeDto.setTotalNumberOfClasses(scheduleFiltered.size());
            Integer numberOfClassesAttended = attendanceMap.get(student);
            numberOfClassesAttended = numberOfClassesAttended == null ? 0 : numberOfClassesAttended;
            tutorFeeDto.setNumberOfClassesAttended(numberOfClassesAttended);
            tutorFeeDto.setFeeAmount(classSessionPrice.longValue() * numberOfClassesAttended);

            tutorFeeDtos.add(tutorFeeDto);
        }

//        tutorFeeDtos.sort(Comparator.comparing(TutorFeeDto::getStudentName));
        return new PageImpl<>(tutorFeeDtos);
    }


}
