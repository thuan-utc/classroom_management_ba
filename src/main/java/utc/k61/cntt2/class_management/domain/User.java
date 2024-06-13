package utc.k61.cntt2.class_management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {
    @NotBlank(message = "username must be not blank!")
    @Size(max = 30)
    @Column(unique = true)
    private String username;

    @NotBlank(message = "password must be not blank!")
    @Size(max = 100)
    private String password;

    @NaturalId
    @NotBlank(message = "email must be not blank!")
    @Size(max = 40)
    @Email
    @Column(unique = true)
    private String email;

    @Size(max = 40)
    private String phone;

    @Column(name = "first_name")
    private String firstName;

    private String surname;

    @Column(name = "last_name")
    private String lastName;

    private LocalDate dob;

    @OneToOne
    private Role role;

    private Boolean active = false;

    @Column(name = "active_code")
    private String activeCode;

    @Column(name = "number_active_attempt") // number attempt to verify email
    private Integer numberActiveAttempt = 0;

    @Column(name = "last_active_attempt")
    private Instant lastActiveAttempt;

    private String note;

    @JsonIgnore
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Classroom> classrooms;
}
