package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.dto.NewClassRequest;
import utc.k61.cntt2.class_management.service.ClassroomService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {
    private final ClassroomService classroomService;

    @Autowired
    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ResponseEntity<?> getAllClassroomForUser() {
        return ResponseEntity.ok(classroomService.getClassroomForCurrentUser());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam Map<String, String> params, Pageable pageable) throws Exception {
        Page<Classroom> page = classroomService.search(params, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<?> createNewClass(@RequestBody NewClassRequest request) {
        return ResponseEntity.ok(classroomService.createNewClass(request));
    }

    @PostMapping("/student")
    public ResponseEntity<?> uploadStudentListCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(classroomService.uploadListCsvStudent(file));
    }

}
