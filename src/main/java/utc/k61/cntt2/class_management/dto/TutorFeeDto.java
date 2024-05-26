package utc.k61.cntt2.class_management.dto;

import lombok.Data;

@Data
public class TutorFeeDto {
    private String studentName;
    private String email;
    private String phone;
    private Integer numberOfClassesAttended;
    private Integer totalNumberOfClasses;
    private Long feeAmount;
}
