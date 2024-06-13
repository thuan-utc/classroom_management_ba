package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.k61.cntt2.class_management.dto.ClassAttendanceDto;
import utc.k61.cntt2.class_management.dto.ExamScoreDto;
import utc.k61.cntt2.class_management.dto.NewExamRequest;
import utc.k61.cntt2.class_management.service.ExamScoreService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/exam-score")
public class ExamScoreController {
    private final ExamScoreService examScoreService;

    @Autowired
    public ExamScoreController(ExamScoreService examScoreService) {
        this.examScoreService = examScoreService;
    }

    @PostMapping
    public ResponseEntity<?> createExam(@Valid @RequestBody NewExamRequest request) {
        return ResponseEntity.ok(examScoreService.createNewExam(request));
    }

    @GetMapping("/{classId}/exam")
    public Page<?> fetchAllExam(@PathVariable Long classId) {
        return examScoreService.fetchAllExam(classId);
    }

    @GetMapping("/{examId}")
    public Page<?> fetchAllExamScore(@PathVariable Long examId) {
        return examScoreService.fetchAllExamScore(examId);
    }

    @PutMapping
    public ResponseEntity<?> saveExamResult(@RequestBody List<ExamScoreDto> examScoreDtos) {
        return ResponseEntity.ok(examScoreService.saveExamResult(examScoreDtos));
    }

    @GetMapping("/student-exam-result")
    public ResponseEntity<?> getStudentAttendanceResult(@RequestParam Long classId) {
        return ResponseEntity.ok(examScoreService.getStudentExamResult(classId));
    }
}
