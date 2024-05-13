package utc.k61.cntt2.class_management.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import utc.k61.cntt2.class_management.domain.User;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UserDetailDto {
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String surname;
    private String lastName;
    private LocalDate dob;
    private String accountType;

    public UserDetailDto(User user) {
        this.username = user.getUsername();
        this.email= user.getEmail();
        this.phone = user.getPhone();
        this.firstName = user.getFirstName();
        this.surname= user.getSurname();
        this.lastName = user.getLastName();
        this.dob = user.getDob();
        this.accountType = user.getRole().getName().toString();
    }
}
