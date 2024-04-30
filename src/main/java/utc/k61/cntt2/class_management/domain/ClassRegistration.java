package utc.k61.cntt2.class_management.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "class_registration")
public class ClassRegistration extends BaseEntity {
    private Instant registerDay;

    private Boolean active;
}
