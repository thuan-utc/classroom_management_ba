package utc.k61.cntt2.class_management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tutor_fee")
public class TutorFee extends BaseEntity {
    private Integer month;
    private Integer year;
    private Integer lessonPrice;
    private Integer totalLesson;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @JsonIgnore
    @OneToMany(mappedBy = "tutorFee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TutorFeeDetail> tutorFeeDetails;

}
