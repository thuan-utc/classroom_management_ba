package utc.k61.cntt2.class_management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamScoreDto {
    private Long id;
    private String name;
    private String email;
    private String dob;
    private Double score;
    private String examName;
}
