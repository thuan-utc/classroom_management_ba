package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.Role;
import utc.k61.cntt2.class_management.domain.RoleName;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.security.SignUpRequest;
import utc.k61.cntt2.class_management.exception.BadRequestException;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.RoleRepository;
import utc.k61.cntt2.class_management.repository.UserRepository;
import utc.k61.cntt2.class_management.security.SecurityUtils;

import java.util.Collections;

@Service
@Log4j2
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createNewUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            String errorMessage = String.format("UserName %s already in use", signUpRequest.getUsername());
            throw new BadRequestException(errorMessage);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            String errorMessage = String.format("Email %s already in use", signUpRequest.getEmail());
            throw new BadRequestException(errorMessage);
        }

        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BusinessException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        return userRepository.save(user);
    }

    public User getUserDetail() {
        String currentLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BusinessException("Not found current user login"));
        User user = userRepository.findByUsername(currentLogin)
                .orElseThrow(() -> new BusinessException("Not found user with login " + currentLogin));
        return user;
    }
}
