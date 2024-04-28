package utc.k61.cntt2.class_management.domain;

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

    @Column(name = "subject_description", columnDefinition = "text")
    private String subjectDescription;

    @Column(name = "class_code")
    private String classCode;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

//    @ManyToMany(mappedBy = "attendedClasses")
//    private List<User> students;
}
