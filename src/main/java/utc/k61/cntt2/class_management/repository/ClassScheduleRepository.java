package utc.k61.cntt2.class_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.k61.cntt2.class_management.domain.ClassSchedule;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long>, JpaSpecificationExecutor<ClassSchedule> {
    List<ClassSchedule> findAllByClassroomIdOrderByDayAsc(Long classId);

    List<ClassSchedule> findAllByClassroomIdInAndDayBetween(List<Long> classIds, LocalDate startOfWeek, LocalDate endOfWeek);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM class_schedule WHERE id = :scheduleId")
    void deleteById(@Param("scheduleId") Long scheduleId);
}
