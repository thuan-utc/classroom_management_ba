package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.dto.StudentDto;
import utc.k61.cntt2.class_management.service.StudentService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam Map<String, String> params, Pageable pageable) {
        Page<ClassRegistration> page = studentService.search(params, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping
    public ResponseEntity<?> getAllStudentForClass(@RequestParam Long classId) {
        List<ClassRegistration> students = studentService.getAllStudentForClass(classId);
        return ResponseEntity.ok(new PageImpl<>(students));
    }

    @PostMapping("/{classId}")
    public ResponseEntity<?> addStudentForClass(@RequestBody StudentDto studentDto, @PathVariable Long classId) {
        return ResponseEntity.ok(studentService.addStudentForClass(studentDto, classId));
    }

    @GetMapping("/{classId}/download")
    public void downloadListStudent(HttpServletResponse response, @PathVariable Long classId) throws IOException {
        String filePath = studentService.extractListStudent(classId);
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

