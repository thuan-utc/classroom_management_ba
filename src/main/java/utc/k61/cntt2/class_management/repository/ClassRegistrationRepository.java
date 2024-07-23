package utc.k61.cntt2.class_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.ClassRegistration;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ClassRegistrationRepository extends JpaSpecificationExecutor<ClassRegistration>,
        JpaRepository<ClassRegistration, Long> {
    List<ClassRegistration> findAllByClassroomIdOrderByLastNameAsc(Long classId);

    List<ClassRegistration> findAllByStudentId(Long id);

    List<ClassRegistration> findAllByEmail(String email);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM class_registration WHERE id = :studentId")
    void deleteById(@Param("studentId") Long documentId);

    Page<ClassRegistration> findAll(Specification<ClassRegistration> spec, Pageable pageable);
}
