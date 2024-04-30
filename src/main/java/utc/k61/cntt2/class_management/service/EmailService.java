package utc.k61.cntt2.class_management.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Deprecated
@Service
public class EmailService {
    private Logger log = LoggerFactory.getLogger(EmailService.class);
    private final String sendgridApiKey;
    private final String emailFrom;
    private final SendGrid sendGrid;

    @Autowired
    public EmailService(@Value("${sendgrid.api.key}") String sendgridApiKey, @Value("${sendgrid.mail.from}") String emailFrom) {
        this.sendgridApiKey = sendgridApiKey;
        this.emailFrom = emailFrom;
        this.sendGrid = new SendGrid(sendgridApiKey);
    }

    public void sendEmail(String to, String subject, String text) {
        Email from = new Email(emailFrom);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.info("Status code: {}", response.getStatusCode());
            log.info("Body: {}", response.getBody());
            log.info("Sent remind bill email to email: {}", to);
        } catch (IOException e) {
            log.error("Can not send email to {}", to, e);
        }
    }

//    @PostConstruct
    public void testSendEmail() {
        sendEmail("thuansl99@gmail.com", "Test send email using SendGrid", "This is test email from thuansl99@gmail.com");
    }
}
