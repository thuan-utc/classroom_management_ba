package utc.k61.cntt2.class_management.dto.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignUpRequest {
    @NotBlank(message = "username must not be empty!")
    @Size(min = 3, max = 15)
    private String username;

    @Email
    @NotBlank(message = "email must not be empty!")
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank(message = "password length must be greater 6!")
    @Size(min = 6, max = 20)
    private String password;
}
