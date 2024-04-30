package utc.k61.cntt2.class_management.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.dto.EmailDetail;

import javax.annotation.PostConstruct;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}") private String sender;

    @Override
    public String sendSimpleEmail(EmailDetail details) {
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            return "Error while Sending Mail";
        }
    }

//    @PostConstruct
    public void testSendEmail() {
        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient("ducthuan2002it@gmail.com");
        emailDetail.setSubject("Test by JavaMailSender");
        emailDetail.setMsgBody("body txt");
        sendSimpleEmail(emailDetail);
    }
}
