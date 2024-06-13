package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.EmailDetail;
import utc.k61.cntt2.class_management.dto.TutorFeeDto;
import utc.k61.cntt2.class_management.service.email.EmailService;
//import utc.k61.cntt2.class_management.repository.TutorFeeRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class TutorFeeService {
    //    private final TutorFeeRepository tutorFeeRepository;
    private final ClassroomService classroomService;
    private final EmailService emailService;
    private final String tempFolder;

    @Autowired
    public TutorFeeService(ClassroomService classroomService,
                           EmailService emailService, @Value("${app.temp}") String tempFolder) {
        this.classroomService = classroomService;
        this.emailService = emailService;
        this.tempFolder = tempFolder;
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

    public Page<TutorFeeDto> calculateFee(Long classId, Integer month, Integer year, Integer classSessionPrice) {
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


    public String extractTutorFeeResult(Long classId, Integer month, Integer year, Integer classSessionPrice) {
        List<TutorFeeDto> tutorFeeDtos = calculateFee(classId, month, year, classSessionPrice).getContent();
        String fileName = tempFolder + "/" + "hoc_phi_" + month.toString() + "_" + year.toString() + "_lop_" + classId + ".xlsx";

        Workbook workbook = new XSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("Students");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Họ tên");
            headerRow.createCell(1).setCellValue("Email");
            headerRow.createCell(2).setCellValue("Phone");
            headerRow.createCell(3).setCellValue("Tổng số buổi");
            headerRow.createCell(4).setCellValue("Số buổi đi học");
            headerRow.createCell(5).setCellValue("Học phí (vnd)");

            // Create data rows
            int rowIndex = 1;
            for (TutorFeeDto tutorFeeDto : tutorFeeDtos) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(tutorFeeDto.getStudentName());
                row.createCell(1).setCellValue(tutorFeeDto.getEmail());
                row.createCell(2).setCellValue(tutorFeeDto.getPhone());
                row.createCell(3).setCellValue(tutorFeeDto.getTotalNumberOfClasses());
                row.createCell(4).setCellValue(tutorFeeDto.getNumberOfClassesAttended());
                row.createCell(5).setCellValue(tutorFeeDto.getFeeAmount());
            }

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            log.error("Error while writing XLSX file", e);
            return null;
        }

        return fileName;
    }

    public String sendTutorFeeNotificationEmail(Long classId, Integer month, Integer year, Integer classSessionPrice) {
        List<TutorFeeDto> tutorFeeDtos = calculateFee(classId, month, year, classSessionPrice).getContent();
        Classroom classroom = classroomService.getById(classId);
        User teacher = classroom.getTeacher();
        String teacherName = teacher.getFirstName() + " " + teacher.getSurname() + " " + teacher.getLastName();
        String teacherEmail = teacher.getEmail();

        for (TutorFeeDto tutorFeeDto : tutorFeeDtos) {
            if (StringUtils.isNotBlank(tutorFeeDto.getEmail())) {
                EmailDetail emailDetail = new EmailDetail();
                emailDetail.setRecipient(tutorFeeDto.getEmail());
                emailDetail.setSubject("Thông tin học phí tháng " + month + "/" + year);
                emailDetail.setMsgBody(buildEmailBody(tutorFeeDto, month, year, teacherName, teacherEmail));

                emailService.sendSimpleEmail(emailDetail);
                log.info("Sent tutor fee notification for email {}", tutorFeeDto.getEmail());
            }
        }

        return "Success";
    }

    private String buildEmailBody(TutorFeeDto tutorFeeDto, Integer month, Integer year, String teacherName, String teacherEmail) {
        return "Kính gửi " + tutorFeeDto.getStudentName() + ",\n\n" +
                "Đây là thông tin học phí của bạn trong tháng " + month + "/" + year + ":\n" +
                "Tổng số buổi học: " + tutorFeeDto.getTotalNumberOfClasses() + "\n" +
                "Số buổi đã tham gia: " + tutorFeeDto.getNumberOfClassesAttended() + "\n" +
                "Số tiền học phí: " + tutorFeeDto.getFeeAmount() + " VND\n\n" +
                "Cảm ơn bạn,\n" +
                "Giáo viên: " + teacherName + "\n" +
                "Liên hệ giáo viên: " + teacherEmail;
    }
}
