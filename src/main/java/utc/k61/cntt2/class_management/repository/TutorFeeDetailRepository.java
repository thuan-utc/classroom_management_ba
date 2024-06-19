package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.TutorFeeDetail;

@Repository
public interface TutorFeeDetailRepository extends JpaRepository<TutorFeeDetail, Long>, JpaSpecificationExecutor<TutorFeeDetail> {
}
