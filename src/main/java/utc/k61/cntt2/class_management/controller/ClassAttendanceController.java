package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.k61.cntt2.class_management.dto.ClassAttendanceDto;
import utc.k61.cntt2.class_management.service.ClassAttendanceService;

import java.util.List;

@RestController
@RequestMapping("/api/class-attendance")
public class ClassAttendanceController {
    private final ClassAttendanceService classAttendanceService;

    @Autowired
    public ClassAttendanceController(ClassAttendanceService classAttendanceService) {
        this.classAttendanceService = classAttendanceService;
    }

    @GetMapping("/{scheduleId}")
    public Page<?> fetchAllClasAttendance(@PathVariable Long scheduleId) {
        return classAttendanceService.fetchClassAttendance(scheduleId);
    }

    @PutMapping
    public ResponseEntity<?> saveAttendanceResult(@RequestBody List<ClassAttendanceDto> attendanceDtoList) {
        return ResponseEntity.ok(classAttendanceService.saveAttendanceResult(attendanceDtoList));
    }

    @GetMapping("/student-attendance-result")
    public ResponseEntity<?> getStudentAttendanceResult(@RequestParam Long classId) {
        return ResponseEntity.ok(classAttendanceService.getStudentAttendanceResult(classId));
    }
}
