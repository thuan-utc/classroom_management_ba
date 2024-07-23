package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.*;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.StudentDto;
import utc.k61.cntt2.class_management.enumeration.RoleName;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class StudentService {
    private final ClassRegistrationRepository classRegistrationRepository;
    private final ClassAttendanceRepository classAttendanceRepository;
    private final TutorFeeDetailRepository tutorFeeDetailRepository;
    private final ClassroomRepository classroomRepository;
    private final UserService userService;
    private final ClassroomService classroomService;
    private final String tempFolder;

    @Autowired
    public StudentService(
            ClassRegistrationRepository classRegistrationRepository,
            ClassAttendanceRepository classAttendanceRepository,
            TutorFeeDetailRepository tutorFeeDetailRepository,
            ClassroomRepository classroomRepository,
            UserService userService,
            ClassroomService classroomService,
            @Value("${app.temp}") String tempFolder) {
        this.classRegistrationRepository = classRegistrationRepository;
        this.classAttendanceRepository = classAttendanceRepository;
        this.tutorFeeDetailRepository = tutorFeeDetailRepository;
        this.classroomRepository = classroomRepository;
        this.userService = userService;
        this.classroomService = classroomService;
        this.tempFolder = tempFolder;
    }

    public List<ClassRegistration> getAllStudentForClass(Long classId) {
        return classRegistrationRepository.findAllByClassroomIdOrderByLastNameAsc(classId);
    }

    public Page<?> search(Map<String, String> params, Pageable pageable) {
        User currentLoginUser = userService.getCurrentUserLogin();
        if (currentLoginUser.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessException("Require Role Teacher!");
        }
        List<Classroom> classrooms = classroomRepository.findAllByTeacherId(currentLoginUser.getId());
        List<Long> classId = classrooms.stream().map(Classroom::getId).collect(Collectors.toList());
        Specification<ClassRegistration> specs = getSpecification(params, classId);
        Page<ClassRegistration> all = classRegistrationRepository.findAll(specs, pageable);
        List<ClassRegistration> students = all.getContent();
        List<StudentDto> studentDtos = new ArrayList<>();
        for (ClassRegistration classRegistration : students) {
            List<TutorFeeDetail> tutorFeeDetails = classRegistration.getTutorFeeDetails();
            Long feeNotSubmitted = 0L;
            for (TutorFeeDetail feeDetail : tutorFeeDetails) {
                TutorFee tutorFee = feeDetail.getTutorFee();
                feeNotSubmitted += (long)tutorFee.getLessonPrice() * feeDetail.getNumberOfAttendedLesson() - feeDetail.getFeeSubmitted();
            }
            Classroom classroom = classRegistration.getClassroom();
            StudentDto studentDto = StudentDto.builder().
                    id(classRegistration.getId())
                    .dob(classRegistration.getDob())
                    .firstName(classRegistration.getFirstName())
                    .surname(classRegistration.getSurname())
                    .lastName(classRegistration.getLastName())
                    .email(classRegistration.getEmail())
                    .phone(classRegistration.getPhone())
                    .note(classRegistration.getNote())
                    .feeNotSubmitted(feeNotSubmitted)
                    .className(classroom.getClassName())
                    .build();
            studentDtos.add(studentDto);
        }
        return new PageImpl<>(studentDtos, pageable, all.getTotalElements());
    }

    private Specification<ClassRegistration> getSpecification(Map<String, String> params, List<Long> classIds) {
        return Specification.where((root, criteriaQuery, criteriaBuilder) -> {
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
                    } else if (StringUtils.equalsIgnoreCase("className", key)) {
                        predicateList.add(criteriaBuilder.like(root.get("classroom").get("className"), "%" + value + "%"));
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

    public void getAllStudent(Map<String, String> params, Pageable pageable) {
        User currentLoginUser = userService.getCurrentUserLogin();
        if (currentLoginUser == null || !currentLoginUser.getRole().getName().equals(RoleName.TEACHER)) {
            throw new BusinessException("Require Role Teacher!");
        }

        List<Classroom> classrooms = classroomRepository.findAllByTeacherId(currentLoginUser.getId());
        List<Long> classroomIds = classrooms.stream().map(Classroom::getId).collect(Collectors.toList());
        Specification<ClassRegistration> spec = getStudentSpecification(params)
                .and((root, query, cb) -> root.get("classroom").get("id").in(classroomIds));

        Page<ClassRegistration> all = classRegistrationRepository.findAll(spec, pageable);
        List<ClassRegistration> classRegistrations = all.getContent();

        List<StudentDto> studentDtos = new ArrayList<>();
        for (ClassRegistration classRegistration : classRegistrations) {
            List<TutorFeeDetail> tutorFeeDetails = classRegistration.getTutorFeeDetails();
            long feeNotSubmitted = 0L;
            for (TutorFeeDetail feeDetail : tutorFeeDetails) {
                TutorFee tutorFee = feeDetail.getTutorFee();
                feeNotSubmitted += (long)tutorFee.getLessonPrice() * feeDetail.getNumberOfAttendedLesson() - feeDetail.getFeeSubmitted();
            }
            Classroom classroom = classRegistration.getClassroom();

            StudentDto studentDto = StudentDto.builder()
                    .id(classRegistration.getId())
                    .dob(classRegistration.getDob())
                    .firstName(classRegistration.getFirstName())
                    .surname(classRegistration.getSurname())
                    .lastName(classRegistration.getLastName())
                    .email(classRegistration.getEmail())
                    .phone(classRegistration.getPhone())
                    .note(classRegistration.getNote())
                    .feeNotSubmitted(feeNotSubmitted)
                    .className(classroom.getClassName())
                    .build();
            studentDtos.add(studentDto);
        }

        new PageImpl<>(studentDtos, pageable, all.getTotalElements());
    }

    public Specification<ClassRegistration> getStudentSpecification(Map<String, String> searchCriteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (searchCriteria.get("firstName") != null && !searchCriteria.get("firstName").isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("firstName"), "%" + searchCriteria.get("firstName") + "%"));
            }
            if (searchCriteria.get("surname") != null && !searchCriteria.get("surname").isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("surname"), "%" + searchCriteria.get("surname") + "%"));
            }
            if (searchCriteria.get("lastName") != null && !searchCriteria.get("lastName").isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("lastName"), "%" + searchCriteria.get("lastName") + "%"));
            }
            if (searchCriteria.get("email") != null && !searchCriteria.get("email").isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("email"), "%" + searchCriteria.get("email") + "%"));
            }
            if (searchCriteria.get("phone") != null && !searchCriteria.get("phone").isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("phone"), "%" + searchCriteria.get("phone") + "%"));
            }
            if (searchCriteria.get("className") != null && !searchCriteria.get("className").isEmpty()) {
                var classroomJoin = root.join("classroom", JoinType.INNER);
                predicate = cb.and(predicate, cb.like(classroomJoin.get("className"), "%" + searchCriteria.get("className") + "%"));
            }

            return predicate;
        };
    }

    public Object addStudentForClass(StudentDto studentDto, Long classId) {
        Classroom classroom = classroomService.getById(classId);
        ClassRegistration student = ClassRegistration.newBuilder()
                .firstName(studentDto.getFirstName())
                .surname(studentDto.getSurname())
                .lastName(studentDto.getLastName())
                .email(studentDto.getEmail())
                .phone(studentDto.getPhone())
                .address(studentDto.getAddress()).build();
        student.setClassroom(classroom);
        if (StringUtils.isNotBlank(student.getEmail())) {
            List<User> users = userService.findAllByEmailIn(List.of(studentDto.getEmail()));
            Optional<User> existingUser = users.stream().filter(user -> StringUtils.equalsIgnoreCase(user.getEmail(), student.getEmail())).findAny();
            existingUser.ifPresent(student::setStudent);
            if (existingUser.isPresent()) {
                student.setStudent(existingUser.get());
            } else {
                if (StringUtils.isNotBlank(student.getEmail())) {
                    try {
                        userService.createDefaultStudentAccount(student);
                    } catch (Exception e) {
                        log.error("Failed to create account for email {}", student.getEmail(), e);
                    }
                }

            }
        }
        classRegistrationRepository.save(student);

        return new ApiResponse(true, "Success");
    }

    public StudentDto getStudentDetail(Long studentId) {
        ClassRegistration classRegistration = classRegistrationRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        List<TutorFeeDetail> tutorFeeDetails = classRegistration.getTutorFeeDetails();
        long feeNotSubmitted = 0L;
        for (TutorFeeDetail feeDetail : tutorFeeDetails) {
            TutorFee tutorFee = feeDetail.getTutorFee();
            feeNotSubmitted += (long)tutorFee.getLessonPrice() * feeDetail.getNumberOfAttendedLesson() - feeDetail.getFeeSubmitted();
        }
        Classroom classroom = classRegistration.getClassroom();

        return StudentDto.builder().
                    id(studentId)
                    .dob(classRegistration.getDob())
                    .firstName(classRegistration.getFirstName())
                    .surname(classRegistration.getSurname())
                    .lastName(classRegistration.getLastName())
                    .email(classRegistration.getEmail())
                    .phone(classRegistration.getPhone())
                    .note(classRegistration.getNote())
                    .feeNotSubmitted(feeNotSubmitted)
                    .className(classroom.getClassName())
                    .build();
    }
}
