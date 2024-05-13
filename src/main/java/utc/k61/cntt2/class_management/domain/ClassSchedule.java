package utc.k61.cntt2.class_management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utc.k61.cntt2.class_management.enumeration.ClassPeriod;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "class_schedule")
public class ClassSchedule extends BaseEntity {
    private LocalDate day;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClassPeriod periodInDay; // ca hoc trong ngay

    private String dayInWeek; // MON, TUE, ...

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
}
