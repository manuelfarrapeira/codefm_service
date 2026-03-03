package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.exception.teachernotebook.SubjectForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SubjectNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SubjectValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.service.teachernotebook.SubjectService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<Subject> getSubjectsByTeacher() {
        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
        return subjectRepository.findByTeacherId(teacherId);
    }

    @Override
    public Subject createSubject(Subject subject) {
        List<ErrorMessage> errors = new ArrayList<>();
        Locale locale = sessionUser.getLocale();

        validateSubject(subject, errors, locale);

        if (!errors.isEmpty()) {
            throw new SubjectValidationException(errors);
        }

        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
        subject.setTeacherId(teacherId);

        return subjectRepository.save(subject);
    }

    @Override
    public Optional<Subject> getSubjectById(Integer subjectId) {
        return subjectRepository.findById(subjectId);
    }

    @Override
    @Transactional
    public void softDeleteSubject(Integer subjectId) {
        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
        Locale locale = sessionUser.getLocale();
        validateSubjectOwnership(subjectId, teacherId, locale);
        subjectRepository.softDeleteSubject(subjectId, teacherId);
    }

    @Override
    @Transactional
    public Subject updateSubject(Integer subjectId, Subject subject) {
        Integer teacherId = sessionUser.getParameter(SessionParameter.TEACHER_ID);
        Locale locale = sessionUser.getLocale();
        List<ErrorMessage> errors = new ArrayList<>();

        validateSubject(subject, errors, locale);

        if (!errors.isEmpty()) {
            throw new SubjectValidationException(errors);
        }

        Subject existingSubject = validateSubjectOwnership(subjectId, teacherId, locale);

        existingSubject.setName(subject.getName());

        return subjectRepository.save(existingSubject);
    }

    private void validateSubject(Subject subject, List<ErrorMessage> errors, Locale locale) {
        if (subject.getName() == null || subject.getName().trim().isEmpty()) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED, null, locale);
            errors.add(new ErrorMessage("name", translatedMessage));
        }
    }

    private Subject validateSubjectOwnership(Integer subjectId, Integer teacherId, Locale locale) {
        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

        if (subjectOpt.isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.SUBJECT_NOT_FOUND, null, locale);
            throw new SubjectNotFoundException(message);
        }

        Subject subject = subjectOpt.get();

        if (!subject.getTeacherId().equals(teacherId)) {
            String message = messageSource.getMessage(MessageKeys.SUBJECT_FORBIDDEN, null, locale);
            throw new SubjectForbiddenException(message);
        }

        return subject;
    }
}
