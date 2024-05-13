package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.service.StudentService;

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
}
