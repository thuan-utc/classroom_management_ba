package utc.k61.cntt2.class_management.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utc.k61.cntt2.class_management.service.UserService;

@RestController
@RequestMapping("/api/user")
@Log4j2
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getUserDetail() {
        return ResponseEntity.ok(userService.getCurrentLoginUserDetail());
    }
}
