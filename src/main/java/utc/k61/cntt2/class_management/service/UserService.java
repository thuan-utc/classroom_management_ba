package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.Role;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.EmailDetail;
import utc.k61.cntt2.class_management.dto.ResetPasswordRequest;
import utc.k61.cntt2.class_management.dto.security.SignUpRequest;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BadRequestException;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.RoleRepository;
import utc.k61.cntt2.class_management.repository.UserRepository;
import utc.k61.cntt2.class_management.security.SecurityUtils;
import utc.k61.cntt2.class_management.service.email.EmailService;

import java.time.*;

@Service
@Log4j2
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void createNewUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            String errorMessage = String.format("Username %s already in use", signUpRequest.getUsername());
            throw new BadRequestException(errorMessage);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            String errorMessage = String.format("Email %s already in use", signUpRequest.getEmail());
            throw new BadRequestException(errorMessage);
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseThrow(() -> new BusinessException("User Role not set."));
        user.setRole(userRole);

        user.setActive(false);
        user.setNumberActiveAttempt(0);
        String activeCode = RandomStringUtils.randomAlphanumeric(6);
        user.setActiveCode(activeCode);

        user = userRepository.save(user);

        sendEmailVerification(user);
    }

    private void sendEmailVerification(User user) {
        EmailDetail emailDetail = new EmailDetail();
        if (StringUtils.isBlank(user.getEmail())) {
            throw new BusinessException("Can not send email verification for email empty!");
        }
        emailDetail.setSubject("Classroom verification");
        emailDetail.setMsgBody("Here is your active code: " + user.getActiveCode());
        emailDetail.setRecipient(user.getEmail());

        log.info("Sending email verification email for email {}", user.getEmail());
        emailService.sendSimpleEmail(emailDetail);
    }

    public User getCurrentLoginUserDetail() {
        String currentLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BusinessException("Not found current user login"));

        return userRepository.findByUsername(currentLogin)
                .orElseThrow(() -> new BusinessException("Not found user with login " + currentLogin));
    }

    public ApiResponse activeAccount(String email, String code) {
        User user = userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new BusinessException("Not found user for email: " + email));

        boolean result = verifyEmail(code, user);

        if (result) {
            user.setActive(true);
            user.setActiveCode(null);
        }
        userRepository.save(user);

        return result ? new ApiResponse(true, "Success") : new ApiResponse(false, "Failed");
    }

    private boolean verifyEmail(String code, User user) {
        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        if (user.getNumberActiveAttempt() >= 3) {
            if (user.getLastActiveAttempt().isAfter(startOfToday)) {
                throw new BusinessException("Only 3 times to verify email a day, please try in next day!");
            } else {
                user.setNumberActiveAttempt(0);
            }
        }
        boolean result = StringUtils.equalsIgnoreCase(code, user.getActiveCode());
        user.setNumberActiveAttempt(user.getNumberActiveAttempt() + 1);
        user.setLastActiveAttempt(Instant.now());

        return result;
    }

    public ApiResponse sendMailForgotPassword(String email) {
        User user = userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new BusinessException("Not found user with email: " + email));

        String activeCode = RandomStringUtils.randomAlphanumeric(6);
        user.setActiveCode(activeCode);
        userRepository.save(user);

        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setSubject("Classroom verification");
        emailDetail.setMsgBody("Here is your code to reset your password: " + user.getActiveCode());
        emailDetail.setRecipient(email);

        log.info("Sending email forgot password for email {}", user.getEmail());
        emailService.sendSimpleEmail(emailDetail);

        return new ApiResponse(true, "Sent email forgot password!");
    }

    public ApiResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findFirstByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Not found user with email: " + request.getEmail()));
        boolean result = verifyEmail(request.getCode(), user);
        if (result) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        userRepository.save(user);
        return result   ? new ApiResponse(true, "Success") : new ApiResponse(false, "Failed");
    }

    public ApiResponse resetPassword(String newPass) {
        User user = getCurrentLoginUserDetail();
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
        return new ApiResponse(true, "Success");
    }


}
