package utc.k61.cntt2.class_management.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.ClassRegistration;
import utc.k61.cntt2.class_management.repository.ClassRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class StudentService {
    private final ClassRegistrationRepository classRegistrationRepository;

    @Autowired
    public StudentService(ClassRegistrationRepository classRegistrationRepository) {
        this.classRegistrationRepository = classRegistrationRepository;
    }

    public List<ClassRegistration> getAllStudentForClass(Long classId) {
        return classRegistrationRepository.findAllByClassroomId(classId);
    }

//    public List<ClassRegistration> getStudentForClass(Long classId) {
//
//    }

}
