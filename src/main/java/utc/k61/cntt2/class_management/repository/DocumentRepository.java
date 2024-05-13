package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.ClassDocument;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<ClassDocument, Long>, JpaSpecificationExecutor<ClassDocument> {
    List<ClassDocument> findAllByClassroomId(Long classId);
}
