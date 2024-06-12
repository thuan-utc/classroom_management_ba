package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.ClassDocument;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<ClassDocument, Long>, JpaSpecificationExecutor<ClassDocument> {
    List<ClassDocument> findAllByClassroomIdOrderByLastModifiedDateDesc(Long classId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM class_document WHERE id = :documentId")
    void deleteById(@Param("documentId") Long documentId);
}
