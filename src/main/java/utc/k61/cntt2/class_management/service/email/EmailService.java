package utc.k61.cntt2.class_management.service.email;

import utc.k61.cntt2.class_management.dto.EmailDetail;

public interface EmailService {
    String sendSimpleEmail(EmailDetail detail);
}
