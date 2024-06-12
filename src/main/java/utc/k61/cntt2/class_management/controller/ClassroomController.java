package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.dto.classroom.ClassroomDto;
import utc.k61.cntt2.class_management.dto.classroom.NewClassRequest;
import utc.k61.cntt2.class_management.service.ClassroomService;

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

    @PutMapping
    public ResponseEntity<?> updateClassDetail(@RequestBody ClassroomDto classroomDto) {
        return ResponseEntity.ok(classroomService.updateClassDetail(classroomDto));
    }

    @PutMapping("/student/{classId}")
    public ResponseEntity<?> uploadStudentListXlsx(@RequestParam("file") MultipartFile file, @PathVariable Long classId) {
        return ResponseEntity.ok(classroomService.uploadListXlsxStudent(file, classId));
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getClassDetail(@RequestParam Long classId) {
        return ResponseEntity.ok(classroomService.getClassDetail(classId));
    }

    @GetMapping("/class-for-student")
    public ResponseEntity<?> searchClassForStudent(@RequestParam Map<String, String> params, Pageable pageable) throws Exception {
            Page<Classroom> page = classroomService.searchClassForStudent(params, pageable);
        return ResponseEntity.ok(page);
    }
}
