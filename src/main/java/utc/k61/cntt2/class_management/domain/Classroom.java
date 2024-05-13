package utc.k61.cntt2.class_management.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class Classroom extends BaseEntity {

    @Column(name = "class_name")
    private String className;

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @JsonIgnoreProperties
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ClassRegistration> classRegistrations;

    @JsonIgnoreProperties
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ClassSchedule> schedules;

}
