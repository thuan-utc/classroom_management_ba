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
import utc.k61.cntt2.class_management.dto.ClassAttendanceDto;
import utc.k61.cntt2.class_management.dto.StudentAttendanceResultDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.exception.ResourceNotFoundException;
import utc.k61.cntt2.class_management.repository.ClassAttendanceRepository;
import utc.k61.cntt2.class_management.repository.ClassScheduleRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ClassAttendanceService {
    private final ClassAttendanceRepository classAttendanceRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final ClassroomService classroomService;
    private final String tempFolder;

    @Autowired
    public ClassAttendanceService(
            ClassAttendanceRepository classAttendanceRepository,
            ClassScheduleRepository classScheduleRepository,
            ClassroomRepository classroomRepository,
            UserService userService, ClassroomService classroomService,
            @Value("${app.temp}") String tempFolder) {
        this.classAttendanceRepository = classAttendanceRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classroomRepository = classroomRepository;
        this.userService = userService;
        this.classroomService = classroomService;
        this.tempFolder = tempFolder;
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

    public String extractAttendanceResult(Long classId) {
        Classroom classroom = classroomService.getById(classId);
        List<ClassSchedule> classSchedules = classroom.getSchedules();
        List<ClassAttendance> classAttendances = classSchedules.stream()
                .flatMap(classSchedule -> classSchedule.getClassAttendances().stream())
                .collect(Collectors.toList());
        List<ClassRegistration> students = classroom.getClassRegistrations();

        String fileName = tempFolder + "/" + "ket_qua_diem_danh_" + classId + ".xlsx";

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
            int i = 7;
            for (ClassSchedule classSchedule : classSchedules) {
                headerRow.createCell(i++).setCellValue(classSchedule.getDay() + "-" + classSchedule.getPeriodInDay().getName());
            }

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
                int j = 7;
                for (ClassSchedule classSchedule : classSchedules) {
                    String result = "";
                    Optional<ClassAttendance> classAttendance = classSchedule.getClassAttendances().stream()
                            .filter(classAttendance1 -> classAttendance1.getClassRegistration().getId().equals(student.getId()))
                            .findAny();
                    if (classAttendance.isPresent()) {
                        result = classAttendance.get().getIsAttended() ? "1" : "0";
                    }
                    row.createCell(j++).setCellValue(result);
                }
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
}
