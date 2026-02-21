package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassValidationException;
import org.web.codefm.domain.exception.teachernotebook.StudentNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.service.teachernotebook.StudentClassService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentClassServiceImpl implements StudentClassService {

    private final StudentClassRepository studentClassRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public void addStudentToClass(Integer classId, Integer studentId) {
        Integer teacherId = getTeacherId();

        validateClassOwnership(classId, teacherId);
        validateStudentOwnership(studentId, teacherId);

        Optional<StudentClass> existingAssociation = studentClassRepository.findByClassIdAndStudentId(classId, studentId);

        if (existingAssociation.isPresent()) {
            StudentClass association = existingAssociation.get();
            if (association.getDeletionDate() == null) {
                throw new StudentClassValidationException(
                        messageSource.getMessage(MessageKeys.STUDENT_ALREADY_IN_CLASS, null, sessionUser.getLocale())
                );
            } else {
                association.setDeletionDate(null);
                studentClassRepository.update(association);
            }
        } else {
            StudentClass newAssociation = StudentClass.builder()
                    .classId(classId)
                    .studentId(studentId)
                    .deletionDate(null)
                    .build();
            studentClassRepository.save(newAssociation);
        }
    }

    @Override
    public void removeStudentFromClass(Integer classId, Integer studentId) {
        Integer teacherId = getTeacherId();

        validateClassOwnership(classId, teacherId);
        validateStudentOwnership(studentId, teacherId);

        StudentClass association = studentClassRepository.findByClassIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new StudentClassNotFoundException(
                        messageSource.getMessage(MessageKeys.STUDENT_NOT_IN_CLASS, null, sessionUser.getLocale())
                ));

        if (association.getDeletionDate() != null) {
            throw new StudentClassNotFoundException(
                    messageSource.getMessage(MessageKeys.STUDENT_NOT_IN_CLASS, null, sessionUser.getLocale())
            );
        }

        studentClassRepository.softDelete(classId, studentId);
    }

    private void validateClassOwnership(Integer classId, Integer teacherId) {
        if (classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId).isEmpty()) {
            throw new ClassNotFoundException(
                    messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, sessionUser.getLocale())
            );
        }
    }

    private void validateStudentOwnership(Integer studentId, Integer teacherId) {
        if (studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId).isEmpty()) {
            throw new StudentNotFoundException(
                    messageSource.getMessage(MessageKeys.STUDENT_NOT_FOUND, null, sessionUser.getLocale())
            );
        }
    }

    private Integer getTeacherId() {
        return Integer.valueOf(
                sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }
}

