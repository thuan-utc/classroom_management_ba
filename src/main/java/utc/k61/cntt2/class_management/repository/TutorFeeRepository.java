package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.TutorFee;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorFeeRepository extends JpaRepository<TutorFee, Long>, JpaSpecificationExecutor<TutorFee> {
    Optional<TutorFee> findFirstByClassroomIdAndYearAndMonth(Long classId, Integer month, Integer year);
}
