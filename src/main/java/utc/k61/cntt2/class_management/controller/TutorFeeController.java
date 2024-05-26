package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import utc.k61.cntt2.class_management.service.TutorFeeService;

import java.util.Map;

@RestController
@RequestMapping("/api/tutor-fee")
public class TutorFeeController {
    private final TutorFeeService tutorFeeService;

    @Autowired
    public TutorFeeController(TutorFeeService tutorFeeService) {
        this.tutorFeeService = tutorFeeService;
    }

//    @GetMapping("/search")
//    public ResponseEntity<?> search(@RequestParam Map<String, String> params, Pageable pageable) throws Exception {
//        return ResponseEntity.ok(tutorFeeService.search(params, pageable));
//    }

    @GetMapping
    public ResponseEntity<?> calculate(
            @RequestParam Long classId,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Integer classSessionPrice) {
        return ResponseEntity.ok(tutorFeeService.calculateFee(classId, month, year, classSessionPrice));
    }
}
