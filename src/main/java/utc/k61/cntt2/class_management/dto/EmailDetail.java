package utc.k61.cntt2.class_management.dto;

import lombok.Data;

@Data
public class EmailDetail {
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
}
