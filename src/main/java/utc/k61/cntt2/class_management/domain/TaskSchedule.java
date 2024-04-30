package utc.k61.cntt2.class_management.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task_schedule")
public class TaskSchedule extends BaseEntity {
    private String name;
    private Boolean active;

    @ManyToOne
    private User teacher;
}
