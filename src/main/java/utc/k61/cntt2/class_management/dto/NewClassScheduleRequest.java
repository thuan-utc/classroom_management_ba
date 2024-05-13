package utc.k61.cntt2.class_management.dto;

import lombok.Data;
import utc.k61.cntt2.class_management.enumeration.ClassPeriod;

import java.time.LocalDate;

@Data
public class NewClassScheduleRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private ClassPeriod periodInDay;
    private String dayInWeek;
    private Long classId;
}
