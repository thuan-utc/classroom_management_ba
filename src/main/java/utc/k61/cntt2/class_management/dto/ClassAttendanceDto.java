package utc.k61.cntt2.class_management.dto;

import lombok.Data;

@Data
public class ClassAttendanceDto {
    private Long id;
    private String name;
    private String email;
    private String dob;
    private Boolean isAttended;
}
