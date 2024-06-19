package utc.k61.cntt2.class_management.dto;

import lombok.Data;

@Data
public class TutorFeeDetailDto {
    private String studentName;
    private String lastName;
    private String email;
    private String phone;
    private Integer numberOfClassesAttended;
    private Integer totalNumberOfClasses;
    private Long feeAmount;
    private Long feeSubmitted;
    private Long feeNotSubmitted;
    private String time;
    private Long id;
    private Integer year;
    private Integer month;
    private Integer lessionPrice;
}
