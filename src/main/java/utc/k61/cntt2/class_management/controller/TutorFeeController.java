package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.k61.cntt2.class_management.service.TutorFeeService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/tutor-fee")
public class TutorFeeController {
    private final TutorFeeService tutorFeeService;

    @Autowired
    public TutorFeeController(TutorFeeService tutorFeeService) {
        this.tutorFeeService = tutorFeeService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam Map<String, String> params, Pageable pageable) throws Exception {
        return ResponseEntity.ok(tutorFeeService.search(params, pageable));
    }

    @GetMapping
    public ResponseEntity<?> getTutorFeeDetail(
            @RequestParam Long tutorFeeId) {
        return ResponseEntity.ok(tutorFeeService.getTutorFeeDetail(tutorFeeId));
    }

    @GetMapping("/student-not-submitted-tutor-fee")
    public ResponseEntity<?> getStudentNotSubmittedTutorFee(@RequestParam Map<String, String> params, Pageable pageable) {
        return ResponseEntity.ok(tutorFeeService.getStudentNotSubmittedTutorFee(params, pageable));
    }

    @GetMapping("/calculate")
    public ResponseEntity<?> calculate(
            @RequestParam Long classId,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Integer classSessionPrice) {
        return ResponseEntity.ok(tutorFeeService.calculateNewFee(classId, month, year, classSessionPrice));
    }

    @GetMapping("/re-calculate")
    public ResponseEntity<?> reCalculate(
            @RequestParam Long tutorFeeId,
            @RequestParam Integer classSessionPrice) {
        return ResponseEntity.ok(tutorFeeService.reCalculateFee(tutorFeeId, classSessionPrice));
    }

    @GetMapping("/{classId}/result")
    public void downloadExamResult(HttpServletResponse response,
                                   @PathVariable Long classId,
                                   @RequestParam Integer month,
                                   @RequestParam Integer year,
                                   @RequestParam Integer classSessionPrice) throws IOException {
        String filePath = tutorFeeService.extractTutorFeeResult(classId, month, year, classSessionPrice);
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

    @GetMapping("/send-tutor-fee-notification")
    public ResponseEntity<?> sendTutorFeeNotification( @RequestParam Long classId,
                                                       @RequestParam Integer month,
                                                       @RequestParam Integer year,
                                                       @RequestParam Integer classSessionPrice) {
        return ResponseEntity.ok(tutorFeeService.sendTutorFeeNotificationEmail(classId, month, year, classSessionPrice));
    }

    @PutMapping("/pay")
    public ResponseEntity<?> sendTutorFeeNotification( @RequestParam Long tutorFeeDetailId) {
        return ResponseEntity.ok(tutorFeeService.pay(tutorFeeDetailId));
    }

    @GetMapping("/fee-for-student")
    public ResponseEntity<?> getTutorFeeForStudent(
            @RequestParam Long classId) {
        return ResponseEntity.ok(tutorFeeService.getTutorFeeForStudent(classId));
    }

    @GetMapping("/test")
    public String testGetTutorFee(){
        return  "";
    }
}
