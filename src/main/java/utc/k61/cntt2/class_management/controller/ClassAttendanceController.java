package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.k61.cntt2.class_management.dto.ClassAttendanceDto;
import utc.k61.cntt2.class_management.service.ClassAttendanceService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    @GetMapping("/{classId}/result")
    public void downloadAttendanceResult(HttpServletResponse response, @PathVariable Long classId) throws IOException {
        String filePath = classAttendanceService.extractAttendanceResult(classId);
        File file = new File(filePath);

        // Check if the file exists
        if (!file.exists()) {
            // If the file doesn't exist, return a 404 error response
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Set the response headers for XLSX
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setContentLength((int) file.length());

        // Stream the file content to the response
        try (FileInputStream fileIn = new FileInputStream(file);
             ServletOutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }


}
