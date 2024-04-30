package utc.k61.cntt2.class_management.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class SignUpRequest {
    @NotBlank(message = "username must not be empty!")
    @Size(min = 3, max = 15)
    private String username;

    @Email
    @NotBlank(message = "email must not be empty!")
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank(message = "password length is less then 6!")
    @Size(min = 6, max = 20)
    private String password;
}
