package utc.k61.cntt2.class_management.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String code;
    @NotBlank
    private String newPassword;
}
