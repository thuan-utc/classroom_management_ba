package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class StudentService {
    private final ClassRegistrationRepository classRegistrationRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;

    @Autowired
    public StudentService(
            ClassRegistrationRepository classRegistrationRepository,
            ClassroomRepository classroomRepository,
            UserService userService) {
        this.classRegistrationRepository = classRegistrationRepository;
        this.classroomRepository = classroomRepository;
        this.userService = userService;
    }

    public List<ClassRegistration> getAllStudentForClass(Long classId) {
        return classRegistrationRepository.findAllByClassroomIdOrderByLastNameAsc(classId);
    }

    public Page<ClassRegistration> search(Map<String, String> params, Pageable pageable) {
        User currentLoginUser = userService.getCurrentUserLogin();
        if (currentLoginUser.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Require Role Teacher!");
        }
        List<Classroom> classrooms = classroomRepository.findAllByTeacherId(currentLoginUser.getId());
        List<Long> classId = classrooms.stream().map(Classroom::getId).collect(Collectors.toList());
        Specification<ClassRegistration> specs = getSpecification(params, classId);
        return classRegistrationRepository.findAll(specs, pageable);
    }

    private Specification<ClassRegistration> getSpecification(Map<String, String> params, List<Long> classIds) {
        return Specification.where((root, criteriaQuery, criteriaBuilder) ->{
            Predicate predicate = null;
            List<Predicate> predicateList = new ArrayList<>();
            for (Map.Entry<String, String> p : params.entrySet()) {
                String key = p.getKey();
                String value = p.getValue();
                if (!"page".equalsIgnoreCase(key) && !"size".equalsIgnoreCase(key) && !"sort".equalsIgnoreCase(key)) {
                    if (StringUtils.equalsIgnoreCase("startCreatedDate", key)) { //"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                        predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toInstant(ZoneOffset.UTC)));
                    } else if (StringUtils.equalsIgnoreCase("endCreatedDate", key)) {
                        predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toInstant(ZoneOffset.UTC)));
                    } else {
                        if (value != null && (value.contains("*") || value.contains("%"))) {
                            predicateList.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                        } else if (value != null) {
                            predicateList.add(criteriaBuilder.like(root.get(key), value + "%"));
                        }
                    }
                }
            }

            predicateList.add(root.get("classroom").get("id").in(classIds));

            if (!predicateList.isEmpty()) {
                predicate = criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }

            return predicate;
        });
    }

}
