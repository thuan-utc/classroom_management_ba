package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.ExamScore;

import java.util.List;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Long> {
    List<ExamScore> findAllByIdIn(List<Long> examScoreIds);
}
