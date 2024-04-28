package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utc.k61.cntt2.class_management.domain.Classroom;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.dto.ApiResponse;
import utc.k61.cntt2.class_management.dto.NewClassRequest;
import utc.k61.cntt2.class_management.exception.BusinessException;
import utc.k61.cntt2.class_management.repository.ClassroomRepository;
import utc.k61.cntt2.class_management.repository.UserRepository;
import utc.k61.cntt2.class_management.security.SecurityUtils;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    @Autowired
    public ClassroomService(ClassroomRepository classroomRepository, UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
    }

    public List<Classroom> getClassroomForCurrentUser() {
        User currentLoginUser = SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .orElseThrow(() -> new BusinessException("Can not find current user login!"));

        //todo specific dto information return for teacher, student
        return classroomRepository.findAllByTeacherId(currentLoginUser.getId());
    }

    public ApiResponse createNewClass(NewClassRequest request) {
        User currentLoginUser = SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .orElseThrow(() -> new BusinessException("Can not find current user login!"));

        //todo check if currentUser has role is teacher
        Classroom newClass = new Classroom();
        newClass.setClassName(request.getClassName());
        newClass.setSubjectDescription(request.getDescription());
        newClass.setTeacher(currentLoginUser);

        classroomRepository.save(newClass);
        log.info("Created new class for teacher with login {}", currentLoginUser.getUsername());
        return new ApiResponse(true, "Created new class successfully");
    }

    public ApiResponse uploadListCsvStudent(MultipartFile f) {
        File file = new File("/temp/" + f.getOriginalFilename());
        try {
            f.transferTo(file);
        } catch (Exception e) {
            log.error("Can not process file", e);
            throw new BusinessException("Cannot process file");
        }

//        log.info("Parsing csv file {}", file.getName());
//        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
//        CsvMapper csvMapper = new CsvMapper();
//        csvMapper.enable(CsvParser.Feature.TRIM_SPACES);
//        csvMapper.addMixIn(PriceRaw.class, PriceRawFormat.class);
//        try {
//            MappingIterator<PriceRaw> priceRawMappingIterator = csvMapper.readerFor(PriceRaw.class).with(csvSchema)
//                    .readValues(file);
//            return priceRawMappingIterator.readAll();
//        } catch (Exception e) {
//            log.error("Failed to process price file {}", file.getName(), e);
//            return new ArrayList<>();
//        }

        return new ApiResponse(true, "Success");

    }

    public Page<Classroom> search(Map<String, String> params, Pageable pageable) {
        Specification<Classroom> specs = getSpecification(params);
        return classroomRepository.findAll(specs, pageable);
    }

    private Specification<Classroom> getSpecification(Map<String, String> params) {
        return Specification.where((root, criteriaQuery, criteriaBuilder) ->{
            Predicate predicate = null;
            List<Predicate> predicateList = new ArrayList<>();
            for (Map.Entry<String, String> p : params.entrySet()) {
                String key = p.getKey();
                String value = p.getValue();
                if (!("page".equalsIgnoreCase(key)) && "size".equalsIgnoreCase(key) && "sort".equalsIgnoreCase(key)) {
                    if (StringUtils.equalsIgnoreCase("startCreatedDate", key)) {
                        predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).toInstant(ZoneOffset.UTC)));
                    } else if (StringUtils.equalsIgnoreCase("endCreatedDate", key)) {
                        predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).toInstant(ZoneOffset.UTC)));
                    } else {
                        if (value != null && (value.contains("*") || value.contains("%"))) {
                            predicateList.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                        } else if (value != null) {
                            predicateList.add(criteriaBuilder.like(root.get(key), value + "%"));
                        }
                    }
                }
            }
            if (!predicateList.isEmpty()) {
                predicate = criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }

            return predicate;
        });
    }
}
