package utc.k61.cntt2.class_management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "class_registration")
public class ClassRegistration extends BaseEntity {
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "surname")
    private String surname;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    private String phone;

    private String address;

    @Column(name = "registration_date")
    private Instant registrationDate;

    private String note;

    @Column(name = "email_confirmed")
    private Boolean emailConfirmed = false; // check that student has click agree in email invitation

    @ManyToOne
    private User user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
}
