package utc.k61.cntt2.class_management.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class User extends BaseEntity {
    private String login;
    private String email;
    private String phone;
    private String  password;
    private Boolean active;

}
