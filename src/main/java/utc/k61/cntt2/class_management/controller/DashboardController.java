package utc.k61.cntt2.class_management.controller;

import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utc.k61.cntt2.class_management.dto.DashboardDataDto;
import utc.k61.cntt2.class_management.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/class-in-week")
    public ResponseEntity<?> getClassInWeek() {
        return ResponseEntity.ok(new PageImpl<>(dashboardService.fetchClassInCurrentWeek()));
    }

    @GetMapping("/dashboard-data")
    public ResponseEntity<DashboardDataDto> getDashboardData() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

}
