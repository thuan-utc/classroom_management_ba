package utc.k61.cntt2.class_management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tutor_fee_detail")
public class TutorFeeDetail extends BaseEntity {
    private Integer numberOfAttendedLesson;
    private Long feeSubmitted;
    private Long feeAmount;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "class_registration_id")
    private ClassRegistration classRegistration;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tutor_fee_id")
    private TutorFee tutorFee;
}
