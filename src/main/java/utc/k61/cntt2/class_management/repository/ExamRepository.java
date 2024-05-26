package utc.k61.cntt2.class_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
}
