package utc.k61.cntt2.class_management.dto.classroom;

import lombok.Data;

import java.time.Instant;

@Data
public class ClassroomDto {
    private Long id;
    private String className;
    private String subjectName;
    private String note;
    private Integer numberOfStudent;
    private Instant createdDate;
    private String createdBy;
    private String teacherName;
}
