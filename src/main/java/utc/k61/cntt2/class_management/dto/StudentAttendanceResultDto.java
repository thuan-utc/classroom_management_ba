package utc.k61.cntt2.class_management.dto;

import lombok.Data;
import utc.k61.cntt2.class_management.enumeration.ClassPeriod;

import java.time.LocalDate;
import java.util.Date;

@Data
public class StudentAttendanceResultDto {
    private LocalDate day;
    private ClassPeriod classPeriod;
    private Boolean attended;
}
