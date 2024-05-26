package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.ClassRegistration;

import java.util.List;

@Repository
public interface ClassRegistrationRepository extends JpaSpecificationExecutor<ClassRegistration>,
        JpaRepository<ClassRegistration, Long> {
    List<ClassRegistration> findAllByClassroomIdOrderByLastNameAsc(Long classId);

    List<ClassRegistration> findAllByStudentId(Long id);

    List<ClassRegistration> findAllByEmail(String email);
}
