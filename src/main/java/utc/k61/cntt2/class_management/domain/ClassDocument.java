package utc.k61.cntt2.class_management.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "class_document")
public class ClassDocument extends BaseEntity {

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "document_link")
    private String documentLink;

    @ManyToOne
    private Classroom classroom;
}
