package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SubjectClassValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.service.teachernotebook.SubjectClassService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectClassServiceImpl implements SubjectClassService {

    private static final String FIELD_SUBJECT_IDS = "subjectIds";

    private final SubjectClassRepository subjectClassRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final ExerciseRepository exerciseRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<Subject> getSubjectsByClassId(Integer classId) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        validateClassOwnership(classId, teacherId, locale);

        return subjectClassRepository.findSubjectsByClassId(classId);
    }

    @Override
    public List<ClassWithSubjects> getAllClassesWithSubjects() {
        Integer teacherId = getTeacherId();
        return subjectClassRepository.findAllClassesWithSubjectsByTeacherId(teacherId);
    }

    @Override
    @Transactional
    public List<Subject> assignSubjectsToClass(Integer classId, List<Integer> subjectIds) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        validateSubjectIdsNotEmpty(subjectIds, errors, locale);
        if (!errors.isEmpty()) {
            throw new SubjectClassValidationException(errors);
        }

        validateClassOwnership(classId, teacherId, locale);

        validateSubjectsOwnership(subjectIds, teacherId, errors, locale);
        validateNoDuplicateAssignments(classId, subjectIds, errors, locale);

        if (!errors.isEmpty()) {
            throw new SubjectClassValidationException(errors);
        }

        List<SubjectClass> subjectClasses = subjectIds.stream()
                .map(subjectId -> SubjectClass.builder()
                        .subjectId(subjectId)
                        .classId(classId)
                        .build())
                .toList();

        subjectClassRepository.saveAll(subjectClasses);

        return subjectClassRepository.findSubjectsByClassId(classId);
    }

    @Override
    @Transactional
    public void removeSubjectsFromClass(Integer classId, List<Integer> subjectIds) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        validateSubjectIdsNotEmpty(subjectIds, errors, locale);
        if (!errors.isEmpty()) {
            throw new SubjectClassValidationException(errors);
        }

        validateClassOwnership(classId, teacherId, locale);

        validateSubjectClassAssociationsExist(classId, subjectIds, errors, locale);
        if (!errors.isEmpty()) {
            throw new SubjectClassValidationException(errors);
        }

        List<Integer> subjectClassIdsToDelete = new ArrayList<>();
        for (Integer subjectId : subjectIds) {
            subjectClassRepository.findIdBySubjectIdAndClassId(subjectId, classId)
                    .ifPresent(subjectClassIdsToDelete::add);
        }

        if (!subjectClassIdsToDelete.isEmpty()) {
            exerciseRepository.softDeleteBySubjectClassIds(subjectClassIdsToDelete);
        }

        subjectClassRepository.softDeleteAll(classId, subjectIds);
    }

    private Integer getTeacherId() {
        return sessionUser.getParameter(SessionParameter.TEACHER_ID, Integer.class);
    }

    private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
        classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, locale);
                    return new ClassForbiddenException(message);
                });
    }

    private void validateSubjectIdsNotEmpty(List<Integer> subjectIds, List<ErrorMessage> errors, Locale locale) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED, null, locale);
            errors.add(new ErrorMessage(FIELD_SUBJECT_IDS, message));
        }
    }

    private void validateSubjectsOwnership(List<Integer> subjectIds, Integer teacherId, List<ErrorMessage> errors, Locale locale) {
        for (Integer subjectId : subjectIds) {
            var subjectOpt = subjectRepository.findByIdAndTeacherId(subjectId, teacherId);
            if (subjectOpt.isEmpty()) {
                var subject = subjectRepository.findById(subjectId);
                String subjectName = subject.map(Subject::getName).orElse(String.valueOf(subjectId));
                String message = messageSource.getMessage(MessageKeys.SUBJECT_CLASS_SUBJECT_NOT_OWNED, new Object[]{subjectName}, locale);
                errors.add(new ErrorMessage(FIELD_SUBJECT_IDS, message));
            }
        }
    }

    private void validateNoDuplicateAssignments(Integer classId, List<Integer> subjectIds, List<ErrorMessage> errors, Locale locale) {
        for (Integer subjectId : subjectIds) {
            boolean exists = subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(subjectId, classId);
            if (exists) {
                var subject = subjectRepository.findById(subjectId);
                String subjectName = subject.map(Subject::getName).orElse(String.valueOf(subjectId));
                String message = messageSource.getMessage(MessageKeys.SUBJECT_CLASS_ALREADY_EXISTS, new Object[]{subjectName}, locale);
                errors.add(new ErrorMessage(FIELD_SUBJECT_IDS, message));
            }
        }
    }

    private void validateSubjectClassAssociationsExist(Integer classId, List<Integer> subjectIds, List<ErrorMessage> errors, Locale locale) {
        for (Integer subjectId : subjectIds) {
            boolean exists = subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(subjectId, classId);
            if (!exists) {
                var subject = subjectRepository.findById(subjectId);
                String subjectName = subject.map(Subject::getName).orElse(String.valueOf(subjectId));
                String message = messageSource.getMessage(MessageKeys.SUBJECT_CLASS_NOT_FOUND, new Object[]{subjectName}, locale);
                errors.add(new ErrorMessage(FIELD_SUBJECT_IDS, message));
            }
        }
    }
}

