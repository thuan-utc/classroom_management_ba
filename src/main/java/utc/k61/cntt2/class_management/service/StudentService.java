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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.StudentDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ClassAttendanceRepository;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class StudentService {
    private final ClassRegistrationRepository classRegistrationRepository;
    private final ClassAttendanceRepository classAttendanceRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final ClassroomService classroomService;
    private final String tempFolder;

    @Autowired
    public StudentService(
            ClassRegistrationRepository classRegistrationRepository,
            ClassAttendanceRepository classAttendanceRepository, ClassroomRepository classroomRepository,
            UserService userService,
            ClassroomService classroomService,
            @Value("${app.temp}") String tempFolder) {
        this.classRegistrationRepository = classRegistrationRepository;
        this.classAttendanceRepository = classAttendanceRepository;
        this.classroomRepository = classroomRepository;
        this.userService = userService;
        this.classroomService = classroomService;
        this.tempFolder = tempFolder;
    }

    public List<ClassRegistration> getAllStudentForClass(Long classId) {
        return classRegistrationRepository.findAllByClassroomIdOrderByLastNameAsc(classId);
    }

    public Page<ClassRegistration> search(Map<String, String> params, Pageable pageable) {
        User currentLoginUser = userService.getCurrentUserLogin();
        if (currentLoginUser.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Require Role Teacher!");
        }
        List<Classroom> classrooms = classroomRepository.findAllByTeacherId(currentLoginUser.getId());
        List<Long> classId = classrooms.stream().map(Classroom::getId).collect(Collectors.toList());
        Specification<ClassRegistration> specs = getSpecification(params, classId);
        return classRegistrationRepository.findAll(specs, pageable);
    }

    private Specification<ClassRegistration> getSpecification(Map<String, String> params, List<Long> classIds) {
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
                    } else {
                        if (value != null && (value.contains("*") || value.contains("%"))) {
                            predicateList.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                        } else if (value != null) {
                            predicateList.add(criteriaBuilder.like(root.get(key), value + "%"));
                        }
                    }
                }
            }

            predicateList.add(root.get("classroom").get("id").in(classIds));

            if (!predicateList.isEmpty()) {
                predicate = criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }

            return predicate;
        });
    }

    public Object addStudentForClass(StudentDto studentDto, Long classId) {
        Classroom classroom = classroomService.getById(classId);
        ClassRegistration student = ClassRegistration.newBuilder()
                .firstName(studentDto.getFirstName())
                .surname(studentDto.getSurname())
                .lastName(studentDto.getLastName())
                .email(studentDto.getEmail())
                .phone(studentDto.getPhone())
                .address(studentDto.getAddress()).build();
        student.setClassroom(classroom);
        List<User> users = userService.findAllByEmailIn(List.of(studentDto.getEmail()));
        Optional<User> existingUser = users.stream().filter(user -> StringUtils.equalsIgnoreCase(user.getEmail(), student.getEmail())).findAny();
        existingUser.ifPresent(student::setStudent);
        if (existingUser.isPresent()) {
            student.setStudent(existingUser.get());
        } else {
            if (StringUtils.isNotBlank(student.getEmail())) {
                try {
                    userService.createDefaultStudentAccount(student);
                } catch (Exception e) {
                    log.error("Failed to create account for email {}", student.getEmail(), e);
                }
            }

        }
        classRegistrationRepository.save(student);

        return new ApiResponse(true, "Success");
    }

    public String extractListStudent(Long classId) {
        List<ClassRegistration> students = getAllStudentForClass(classId);
        String fileName = tempFolder + "/" + "students_class_" + classId + ".xlsx";

        Workbook workbook = new XSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("Students");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Tên Họ");
            headerRow.createCell(2).setCellValue("Tên Đệm");
            headerRow.createCell(3).setCellValue("Tên");
            headerRow.createCell(4).setCellValue("Email");
            headerRow.createCell(5).setCellValue("Số điện thoại");
            headerRow.createCell(6).setCellValue("Địa chỉ");

            // Create data rows
            int rowIndex = 1;
            for (ClassRegistration student : students) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getSurname());
                row.createCell(3).setCellValue(student.getLastName());
                row.createCell(4).setCellValue(student.getEmail());
                row.createCell(5).setCellValue(student.getPhone());
                row.createCell(6).setCellValue(student.getAddress());
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

    @Transactional
    public ApiResponse deleteStudent(Long studentId) {
        User user = userService.getCurrentUserLogin();
        if (user.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Missing permission");
        }
        List<Classroom> classrooms = user.getClassrooms();
        List<ClassRegistration> classRegistrations = classrooms.stream().flatMap(classroom -> classroom.getClassRegistrations().stream()).collect(Collectors.toList());
        classRegistrations.stream()
                .filter(student -> student.getId().equals(studentId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not found student"));
        try {
            classAttendanceRepository.deleteAllByClassRegistrationId(studentId);
            classRegistrationRepository.deleteById(studentId);
        } catch (Exception e) {
            log.error("Exception during delete operation", e);
            throw new BusinessException("Deletion failed due to an error");
        }

        return new ApiResponse(true,"Success");
    }

    public Object updateStudent(StudentDto studentDto) {
        ClassRegistration existingStudent = classRegistrationRepository.findById(studentDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentDto.getDob()));

        // Update the fields
        existingStudent.setFirstName(studentDto.getFirstName());
        existingStudent.setSurname(studentDto.getSurname());
        existingStudent.setLastName(studentDto.getLastName());
        existingStudent.setEmail(studentDto.getEmail());
        existingStudent.setPhone(studentDto.getPhone());
        existingStudent.setAddress(studentDto.getAddress());
        existingStudent.setDob(studentDto.getDob());

        classRegistrationRepository.save(existingStudent);

        return "Success";
    }
}
