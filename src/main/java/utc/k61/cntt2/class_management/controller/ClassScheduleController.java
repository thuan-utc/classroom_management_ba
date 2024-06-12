package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.ClassSchedule;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.NewClassScheduleRequest;
import utc.k61.cntt2.class_management.service.ClassScheduleService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/class-schedule")
public class ClassScheduleController {
    private final ClassScheduleService classScheduleService;

    @Autowired
    public ClassScheduleController(ClassScheduleService classScheduleService) {
        this.classScheduleService = classScheduleService;
    }

    @GetMapping
    public ResponseEntity<?> getAllClassSchedule(@RequestParam Long classId) throws Exception {
        List<ClassSchedule> students = classScheduleService.getAllClassSchedule(classId);
        return ResponseEntity.ok(new PageImpl<>(students));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam Map<String, String> params, Pageable pageable) throws Exception {
        return ResponseEntity.ok(classScheduleService.search(params, pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createClassSchedule(@RequestBody NewClassScheduleRequest request) {
        return ResponseEntity.ok(classScheduleService.createClassSchedule(request));
    }

    @PutMapping("/delete/{scheduleId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(classScheduleService.deleteSchedule(scheduleId));
    }
}
