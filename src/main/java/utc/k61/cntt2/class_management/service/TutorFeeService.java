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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.*;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.TutorFeeDetailRepository;
import utc.k61.cntt2.class_management.repository.TutorFeeRepository;
import utc.k61.cntt2.class_management.service.email.EmailService;
//import utc.k61.cntt2.class_management.repository.TutorFeeRepository;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class TutorFeeService {
    private final TutorFeeRepository tutorFeeRepository;
    private final TutorFeeDetailRepository tutorFeeDetailRepository;
    private final ClassroomService classroomService;
    private final UserService userService;
    private final EmailService emailService;
    private final String tempFolder;

    @Autowired
    public TutorFeeService(
            TutorFeeRepository tutorFeeRepository,
            TutorFeeDetailRepository tutorFeeDetailRepository,
            ClassroomService classroomService,
            UserService userService,
            EmailService emailService,
            @Value("${app.temp}") String tempFolder) {
        this.tutorFeeRepository = tutorFeeRepository;
        this.tutorFeeDetailRepository = tutorFeeDetailRepository;
        this.classroomService = classroomService;
        this.userService = userService;
        this.emailService = emailService;
        this.tempFolder = tempFolder;
    }


    public Page<TutorFeeDto> search(Map<String, String> params, Pageable pageable) {
        Specification<TutorFee> specs = getSpecification(params);
        List<TutorFee> tutorFees = tutorFeeRepository.findAll(specs, pageable).getContent();
        List<TutorFeeDto> tutorFeeDtos = new ArrayList<>();
        for (TutorFee tutorFee : tutorFees) {

            List<TutorFeeDetail> tutorFeeDetails = tutorFee.getTutorFeeDetails();
            int numberOfAttendance = tutorFeeDetails.stream().map(TutorFeeDetail::getNumberOfAttendedLesson).mapToInt(i -> i).sum();
            Long feeEstimate = tutorFeeDetails.stream().map(TutorFeeDetail::getFeeAmount).mapToLong(i -> i).sum();;// (long) tutorFee.getLessonPrice() * tutorFee.getTotalLesson();
            Long feeCollected = tutorFee.getTutorFeeDetails().stream().map(TutorFeeDetail::getFeeSubmitted).mapToLong(i -> i).sum();
            Long feeNotCollected = feeEstimate - feeCollected;
            TutorFeeDto tutorFeeDto = TutorFeeDto.builder()
                    .createdDate(tutorFee.getCreatedDate())
                    .id(tutorFee.getId())
                    .year(tutorFee.getYear())
                    .month(tutorFee.getMonth())
                    .lessonPrice(tutorFee.getLessonPrice())
                    .totalLesson(tutorFee.getTotalLesson())
                    .feeEstimate(feeEstimate)
                    .feeCollected(feeCollected)
                    .feeNotCollected(feeNotCollected)
                    .build();

            tutorFeeDtos.add(tutorFeeDto);
        }

        return new PageImpl<>(tutorFeeDtos);
    }

    public Page<TutorFeeDetailNotSubmittedDto> getStudentNotSubmittedTutorFee(Map<String, String> params, Pageable pageable) {
        User user = userService.getCurrentUserLogin();
        List<Classroom> classrooms = user.getClassrooms();

        List<Long> classroomIds = classrooms.stream()
                .map(Classroom::getId)
                .collect(Collectors.toList());

        Specification<TutorFeeDetail> spec = hasSearchCriteria(params)
                .and((root, query, cb) -> root.get("tutorFee").get("classroom").get("id").in(classroomIds))
                .and((root, query, cb) -> cb.lessThan(root.get("feeSubmitted"), root.get("feeAmount")));

        Page<TutorFeeDetail> all = tutorFeeDetailRepository.findAll(spec, pageable);
        List<TutorFeeDetail> unpaidTutorFeeDetails = all.getContent();

        List<TutorFeeDetailNotSubmittedDto> students = unpaidTutorFeeDetails.stream()
                .map(this::convertToFeeNotSubmittedDto)
                .collect(Collectors.toList());

        return new PageImpl<>(students, pageable, all.getTotalElements());
    }

    private TutorFeeDetailNotSubmittedDto convertToFeeNotSubmittedDto(TutorFeeDetail tutorFeeDetail) {
        ClassRegistration student = tutorFeeDetail.getClassRegistration();
        Classroom classroom = tutorFeeDetail.getTutorFee().getClassroom();

        return TutorFeeDetailNotSubmittedDto.builder()
                .firstName(student.getFirstName())
                .surname(student.getSurname())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(student.getAddress())
                .dob(student.getDob())
                .id(tutorFeeDetail.getId())
                .className(classroom.getClassName())
                .feeNotSubmitted(tutorFeeDetail.getFeeAmount() - tutorFeeDetail.getFeeSubmitted())
                .note(student.getNote())
                .year(tutorFeeDetail.getTutorFee().getYear())
                .month( tutorFeeDetail.getTutorFee().getMonth())
                .build();
    }

    public Specification<TutorFeeDetail> hasSearchCriteria(Map<String, String> searchCriteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Join with related entities
            var classRegistrationJoin = root.join("classRegistration", JoinType.INNER);
            var classroomJoin = root.join("tutorFee", JoinType.INNER).join("classroom", JoinType.INNER);

            if (searchCriteria.get("firstName") != null && !searchCriteria.get("firstName").isEmpty()) {
                predicate = cb.and(predicate, cb.like(classRegistrationJoin.get("firstName"), "%" + searchCriteria.get("firstName") + "%"));
            }
            if (searchCriteria.get("surname") != null && !searchCriteria.get("surname").isEmpty()) {
                predicate = cb.and(predicate, cb.like(classRegistrationJoin.get("surname"), "%" + searchCriteria.get("surname") + "%"));
            }
            if (searchCriteria.get("lastName") != null && !searchCriteria.get("lastName").isEmpty()) {
                predicate = cb.and(predicate, cb.like(classRegistrationJoin.get("lastName"), "%" + searchCriteria.get("lastName") + "%"));
            }
            if (searchCriteria.get("email") != null && !searchCriteria.get("email").isEmpty()) {
                predicate = cb.and(predicate, cb.like(classRegistrationJoin.get("email"), "%" + searchCriteria.get("email") + "%"));
            }
            if (searchCriteria.get("phone") != null && !searchCriteria.get("phone").isEmpty()) {
                predicate = cb.and(predicate, cb.like(classRegistrationJoin.get("phone"), "%" + searchCriteria.get("phone") + "%"));
            }
            if (searchCriteria.get("className") != null && !searchCriteria.get("className").isEmpty()) {
                predicate = cb.and(predicate, cb.like(classroomJoin.get("name"), "%" + searchCriteria.get("className") + "%"));
            }

            return predicate;
        };
    }


    private Specification<TutorFee> getSpecification(Map<String, String> params) {
        return Specification.where((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = null;
            List<Predicate> predicateList = new ArrayList<>();
            for (Map.Entry<String, String> p : params.entrySet()) {
                String key = p.getKey();
                String value = p.getValue();
                if (!"page".equalsIgnoreCase(key) && !"size".equalsIgnoreCase(key) && !"sort".equalsIgnoreCase(key)) {
                    if (StringUtils.equalsIgnoreCase("startCreatedDate", key)) { //"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                        predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"),
                                LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        .atStartOfDay()
                                        .toInstant(ZoneOffset.UTC)));
                    } else if (StringUtils.equalsIgnoreCase("endCreatedDate", key)) {
                        predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"),
                                LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        .atStartOfDay()
                                        .toInstant(ZoneOffset.UTC)));
                    } else if (StringUtils.equalsIgnoreCase("classId", key)) {
                        predicateList.add(criteriaBuilder.equal(root.get("classroom").get("id"), Long.valueOf(value)));
                    } else if (StringUtils.equalsIgnoreCase("year", key)) {
                        predicateList.add(criteriaBuilder.equal(root.get("year"), Integer.valueOf(value)));
                    } else if (StringUtils.equalsIgnoreCase("month", key)) {
                        predicateList.add(criteriaBuilder.equal(root.get("month"), Integer.valueOf(value)));
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

    public Page<TutorFeeDetailDto> calculateFee(Long classId, Integer month, Integer year, Integer classSessionPrice) {
        Classroom classroom = classroomService.getById(classId);
        Optional<TutorFee> existTutorFeeOpt = tutorFeeRepository.findFirstByClassroomIdAndYearAndMonth(classId, year, month);

        List<TutorFeeDetail> tutorFeeDetails;
        TutorFee tutorFee;
        if (existTutorFeeOpt.isEmpty()) {
            List<ClassSchedule> classSchedules = classroom.getSchedules();
            List<ClassSchedule> scheduleFiltered = classSchedules.stream()
                    .filter(classSchedule -> classSchedule.getDay().getYear() == year
                            && classSchedule.getDay().getMonth().getValue() == month)
                    .collect(Collectors.toList());
            tutorFee = createNewTutorFee(month, year, classSessionPrice, classroom, scheduleFiltered.size());
            tutorFeeDetails = calculateTutorFeeDetail(tutorFee, scheduleFiltered, classroom.getClassRegistrations());
        } else {
            tutorFee = existTutorFeeOpt.get();
            tutorFeeDetails = existTutorFeeOpt.get().getTutorFeeDetails();
        }

        List<TutorFeeDetailDto> tutorFeeDetailDtos = buildTutorFeeDto(tutorFeeDetails, tutorFee);

        return new PageImpl<>(tutorFeeDetailDtos);
    }

    public TutorFee calculateNewFee(Long classId, Integer month, Integer year, Integer classSessionPrice) {
        Classroom classroom = classroomService.getById(classId);
        Optional<TutorFee> existTutorFeeOpt = tutorFeeRepository.findFirstByClassroomIdAndYearAndMonth(classId, year, month);

        List<TutorFeeDetail> tutorFeeDetails;
        TutorFee tutorFee;
        if (existTutorFeeOpt.isPresent()) {
            throw new BusinessException("Existed!");
        }
        List<ClassSchedule> classSchedules = classroom.getSchedules();
        List<ClassSchedule> scheduleFiltered = classSchedules.stream()
                .filter(classSchedule -> classSchedule.getDay().getYear() == year
                        && classSchedule.getDay().getMonth().getValue() == month)
                .collect(Collectors.toList());
        tutorFee = createNewTutorFee(month, year, classSessionPrice, classroom, scheduleFiltered.size());
        tutorFeeDetails = calculateTutorFeeDetail(tutorFee, scheduleFiltered, classroom.getClassRegistrations());
        return tutorFee;
    }

    private List<TutorFeeDetail> calculateTutorFeeDetail(TutorFee tutorFee, List<ClassSchedule> scheduleFiltered, List<ClassRegistration> classRegistrations1) {
        Map<ClassRegistration, Integer> attendanceMap = extractStudentAttendanceResult(scheduleFiltered);

        List<TutorFeeDetail> tutorFeeDetails = new ArrayList<>();
        for (ClassRegistration student : classRegistrations1) {
            TutorFeeDetail tutorFeeDetail = new TutorFeeDetail();
            tutorFeeDetail.setClassRegistration(student);
            tutorFeeDetail.setTutorFee(tutorFee);
            Integer numberOfAttendedLesson = attendanceMap.get(student);
            numberOfAttendedLesson = numberOfAttendedLesson == null ? 0 : numberOfAttendedLesson;
            tutorFeeDetail.setNumberOfAttendedLesson(numberOfAttendedLesson);
            tutorFeeDetail.setFeeSubmitted(0L);
            tutorFeeDetail.setFeeAmount((long) tutorFee.getLessonPrice() * numberOfAttendedLesson);

            tutorFeeDetails.add(tutorFeeDetail);
        }

        tutorFeeDetails = tutorFeeDetailRepository.saveAll(tutorFeeDetails);
        return tutorFeeDetails;
    }

    private static Map<ClassRegistration, Integer> extractStudentAttendanceResult(List<ClassSchedule> scheduleFiltered) {
        Map<ClassRegistration, Integer> attendanceMap = new HashMap<>();
        for (ClassSchedule schedule : scheduleFiltered) {
            List<ClassAttendance> attendances = schedule.getClassAttendances();
            for (ClassAttendance attendance : attendances) {
                ClassRegistration registration = attendance.getClassRegistration();
                boolean isAttended = attendance.getIsAttended() != null && attendance.getIsAttended();
                attendanceMap.put(registration, attendanceMap.getOrDefault(registration, 0) + (isAttended ? 1 : 0));
            }
        }
        return attendanceMap;
    }

    private TutorFee createNewTutorFee(
            Integer month,
            Integer year,
            Integer classSessionPrice,
            Classroom classroom,
            int totalLesson) {
        TutorFee tutorFee = new TutorFee();
        tutorFee.setClassroom(classroom);
        tutorFee.setYear(year);
        tutorFee.setMonth(month);
        tutorFee.setLessonPrice(classSessionPrice);
        tutorFee.setTotalLesson(totalLesson);
        tutorFee = tutorFeeRepository.save(tutorFee);
        return tutorFee;
    }

    private List<TutorFeeDetailDto>  buildTutorFeeDto(List<TutorFeeDetail> tutorFeeDetails, TutorFee tutorFee) {
        List<TutorFeeDetailDto> tutorFeeDetailDtos = new ArrayList<>();
        for (TutorFeeDetail tutorFeeDetail : tutorFeeDetails) {
            TutorFeeDetailDto tutorFeeDetailDto = new TutorFeeDetailDto();
            ClassRegistration student = tutorFeeDetail.getClassRegistration();
            String studentName = student.getFirstName() + " " + student.getSurname() + " " + student.getLastName();
            tutorFeeDetailDto.setStudentName(studentName);
            tutorFeeDetailDto.setLastName(student.getLastName());
            tutorFeeDetailDto.setEmail(student.getEmail());
            tutorFeeDetailDto.setPhone(student.getPhone());
            tutorFeeDetailDto.setTotalNumberOfClasses(tutorFee.getTotalLesson());
            tutorFeeDetailDto.setNumberOfClassesAttended(tutorFeeDetail.getNumberOfAttendedLesson());
            long feeAmount = (long) tutorFeeDetail.getNumberOfAttendedLesson() * tutorFee.getLessonPrice();
            tutorFeeDetailDto.setFeeAmount(feeAmount);
            tutorFeeDetailDto.setFeeSubmitted(tutorFeeDetail.getFeeSubmitted());
            tutorFeeDetailDto.setFeeNotSubmitted(feeAmount - tutorFeeDetail.getFeeSubmitted());
            tutorFeeDetailDto.setId(tutorFeeDetail.getId());
            tutorFeeDetailDto.setTime(tutorFee.getYear().toString() + "-" + tutorFee.getMonth());

            tutorFeeDetailDtos.add(tutorFeeDetailDto);
        }

        tutorFeeDetailDtos.sort(Comparator.comparing(TutorFeeDetailDto::getLastName));
        return tutorFeeDetailDtos;
    }

    public String extractTutorFeeResult(Long classId, Integer month, Integer year, Integer classSessionPrice) {
        List<TutorFeeDetailDto> tutorFeeDetailDtos = calculateFee(classId, month, year, classSessionPrice).getContent();
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
            for (TutorFeeDetailDto tutorFeeDetailDto : tutorFeeDetailDtos) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(tutorFeeDetailDto.getStudentName());
                row.createCell(1).setCellValue(tutorFeeDetailDto.getEmail());
                row.createCell(2).setCellValue(tutorFeeDetailDto.getPhone());
                row.createCell(3).setCellValue(tutorFeeDetailDto.getTotalNumberOfClasses());
                row.createCell(4).setCellValue(tutorFeeDetailDto.getNumberOfClassesAttended());
                row.createCell(5).setCellValue(tutorFeeDetailDto.getFeeAmount());
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
        List<TutorFeeDetailDto> tutorFeeDetailDtos = calculateFee(classId, month, year, classSessionPrice).getContent();
        Classroom classroom = classroomService.getById(classId);
        User teacher = classroom.getTeacher();
        String teacherName = teacher.getFirstName() + " " + teacher.getSurname() + " " + teacher.getLastName();
        String teacherEmail = teacher.getEmail();

        for (TutorFeeDetailDto tutorFeeDetailDto : tutorFeeDetailDtos) {
            if (StringUtils.isNotBlank(tutorFeeDetailDto.getEmail())) {
                EmailDetail emailDetail = new EmailDetail();
                emailDetail.setRecipient(tutorFeeDetailDto.getEmail());
                emailDetail.setSubject("Thông tin học phí tháng " + month + "/" + year);
                emailDetail.setMsgBody(buildEmailBody(tutorFeeDetailDto, month, year, teacherName, teacherEmail));

                emailService.sendSimpleEmail(emailDetail);
                log.info("Sent tutor fee notification for email {}", tutorFeeDetailDto.getEmail());
            }
        }

        return "Success";
    }

    private String buildEmailBody(TutorFeeDetailDto tutorFeeDetailDto, Integer month, Integer year, String teacherName, String teacherEmail) {
        return "Kính gửi " + tutorFeeDetailDto.getStudentName() + ",\n\n" +
                "Đây là thông tin học phí của bạn trong tháng " + month + "/" + year + ":\n" +
                "Tổng số buổi học: " + tutorFeeDetailDto.getTotalNumberOfClasses() + "\n" +
                "Số buổi đã tham gia: " + tutorFeeDetailDto.getNumberOfClassesAttended() + "\n" +
                "Số tiền học phí: " + tutorFeeDetailDto.getFeeAmount() + " VND\n\n" +
                "Vui lòng bỏ qua nếu đã đóng,\n" +
                "Cảm ơn bạn,\n" +
                "Giáo viên: " + teacherName + "\n" +
                "Liên hệ giáo viên: " + teacherEmail;
    }

    @Transactional
    public TutorFee reCalculateFee(Long tutorFeeId, Integer classSessionPrice) {
        // calculcate new, update with exiting feeSubmited
        TutorFee tutorFee = tutorFeeRepository.findById(tutorFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found tutorFee with id " + tutorFeeId));
        tutorFee.setLessonPrice(classSessionPrice);
        Classroom classroom = tutorFee.getClassroom();
        List<ClassSchedule> classSchedules = classroom.getSchedules();
        List<ClassSchedule> scheduleFiltered = classSchedules.stream()
                .filter(classSchedule -> classSchedule.getDay().getYear() == tutorFee.getYear()
                        && classSchedule.getDay().getMonth().getValue() == tutorFee.getMonth())
                .collect(Collectors.toList());
        tutorFee.setTotalLesson(scheduleFiltered.size());
        tutorFeeRepository.save(tutorFee);
        List<TutorFeeDetail> existFeeDetails = tutorFee.getTutorFeeDetails();
        List<TutorFeeDetail> tutorFeeDetails = reCalculateTutorFeeDetail(tutorFee, scheduleFiltered, classroom.getClassRegistrations(), existFeeDetails);

        return tutorFee;
    }

    private List<TutorFeeDetail> reCalculateTutorFeeDetail(TutorFee tutorFee, List<ClassSchedule> scheduleFiltered, List<ClassRegistration> classRegistrations, List<TutorFeeDetail> existFeeDetails) {
        Map<ClassRegistration, Integer> attendanceMap = extractStudentAttendanceResult(scheduleFiltered);
        List<ClassRegistration> studentCalculated = existFeeDetails.stream().map(TutorFeeDetail::getClassRegistration).collect(Collectors.toList());
        List<ClassRegistration> newStudent = new ArrayList<>();
        for (ClassRegistration classRegistration : classRegistrations) {
            if (!studentCalculated.contains(classRegistration)) {
                newStudent.add(classRegistration);
            }
        }
        List<TutorFeeDetail> tutorFeeDetails = new ArrayList<>();

        // new student calculation
        for (ClassRegistration student : newStudent) {
            TutorFeeDetail tutorFeeDetail = new TutorFeeDetail();
            tutorFeeDetail.setClassRegistration(student);
            tutorFeeDetail.setTutorFee(tutorFee);
            Integer numberOfAttendedLesson = attendanceMap.get(student);
            numberOfAttendedLesson = numberOfAttendedLesson == null ? 0 : numberOfAttendedLesson;
            tutorFeeDetail.setNumberOfAttendedLesson(numberOfAttendedLesson);
            tutorFeeDetail.setFeeSubmitted(0L);
            tutorFeeDetail.setFeeAmount((long) tutorFee.getLessonPrice() * numberOfAttendedLesson);

            tutorFeeDetails.add(tutorFeeDetail);
        }

        //update for existing
        //fill feeSubmitted
        for (TutorFeeDetail tutorFeeDetail : existFeeDetails) {
            Integer numberOfAttendedLesson = attendanceMap.get(tutorFeeDetail.getClassRegistration());
            numberOfAttendedLesson = numberOfAttendedLesson == null ? 0 : numberOfAttendedLesson;
            tutorFeeDetail.setNumberOfAttendedLesson(numberOfAttendedLesson);
            long feeAmount = (long) tutorFeeDetail.getNumberOfAttendedLesson() * tutorFee.getLessonPrice();
            tutorFeeDetail.setFeeAmount(feeAmount);
            tutorFeeDetail.setFeeSubmitted(tutorFeeDetail.getFeeSubmitted());
        }

        tutorFeeDetails.addAll(existFeeDetails);
        tutorFeeDetailRepository.saveAll(tutorFeeDetails);

        return tutorFeeDetails;
    }

    public Page<TutorFeeDetailDto> getTutorFeeDetail(Long tutorFeeId) {
        TutorFee existTutorFee = tutorFeeRepository.findById(tutorFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found tutorFee with id " + tutorFeeId));
        List<TutorFeeDetailDto> tutorFeeDetailDtos = buildTutorFeeDto(existTutorFee.getTutorFeeDetails(), existTutorFee);
        return new PageImpl<>(tutorFeeDetailDtos);
    }

    public Object pay(Long tutorFeeDetailId) {
        User user = userService.getCurrentUserLogin();
        if (user.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Not have permission");
        }
        TutorFeeDetail tutorFeeDetail = tutorFeeDetailRepository.findById(tutorFeeDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found information"));
        tutorFeeDetail.setFeeSubmitted(tutorFeeDetail.getFeeAmount());
        tutorFeeDetailRepository.save(tutorFeeDetail);
        return new ApiResponse(true, "Success");
    }

    public Object getTutorFeeForStudent(Long classId) {
        User user = userService.getCurrentUserLogin();
        if (user.getRole().getName() != RoleName.STUDENT) {
            throw new BusinessException("Not have permission");
        }
        Classroom classroom = classroomService.getById(classId);
        ClassRegistration classRegistration = classroom.getClassRegistrations().stream()
                .filter(classRegistration1 -> classRegistration1.getStudent() != null && classRegistration1.getStudent().equals(user))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found info"));
        List<TutorFeeDetail> tutorFeeDetails = classRegistration.getTutorFeeDetails();
        List<TutorFeeDetailDto> tutorFeeDetailDtos = buildTutorFeeDto(tutorFeeDetails);
        return new PageImpl<>(tutorFeeDetailDtos);
    }

    private List<TutorFeeDetailDto>  buildTutorFeeDto(List<TutorFeeDetail> tutorFeeDetails) {
        List<TutorFeeDetailDto> tutorFeeDetailDtos = new ArrayList<>();
        for (TutorFeeDetail tutorFeeDetail : tutorFeeDetails) {
            TutorFee tutorFee = tutorFeeDetail.getTutorFee();
            TutorFeeDetailDto tutorFeeDetailDto = new TutorFeeDetailDto();
            ClassRegistration student = tutorFeeDetail.getClassRegistration();
            String studentName = student.getFirstName() + " " + student.getSurname() + " " + student.getLastName();
            tutorFeeDetailDto.setStudentName(studentName);
            tutorFeeDetailDto.setLastName(student.getLastName());
            tutorFeeDetailDto.setEmail(student.getEmail());
            tutorFeeDetailDto.setPhone(student.getPhone());
            tutorFeeDetailDto.setTotalNumberOfClasses(tutorFee.getTotalLesson());
            tutorFeeDetailDto.setNumberOfClassesAttended(tutorFeeDetail.getNumberOfAttendedLesson());
            long feeAmount = (long) tutorFeeDetail.getNumberOfAttendedLesson() * tutorFee.getLessonPrice();
            tutorFeeDetailDto.setFeeAmount(feeAmount);
            tutorFeeDetailDto.setFeeSubmitted(tutorFeeDetail.getFeeSubmitted());
            tutorFeeDetailDto.setFeeNotSubmitted(feeAmount - tutorFeeDetail.getFeeSubmitted());
            tutorFeeDetailDto.setId(tutorFeeDetail.getId());
            tutorFeeDetailDto.setMonth(tutorFee.getMonth());
            tutorFeeDetailDto.setYear(tutorFee.getYear());
            tutorFeeDetailDto.setLessionPrice(tutorFee.getLessonPrice());

            tutorFeeDetailDtos.add(tutorFeeDetailDto);
        }

        tutorFeeDetailDtos.sort(Comparator.comparing(TutorFeeDetailDto::getLastName));
        return tutorFeeDetailDtos;
    }
}
