package utc.k61.cntt2.class_management.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tutor_fee")
public class TutorFee extends BaseEntity {
    private Long feeAmount;

    @ManyToOne
    private Classroom classroom;
}
