package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.domain.service.teachernotebook.SchoolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final MessageSource messageSource;

    @Override
    public List<School> getSchoolsByTeacherId(Integer teacherId) {
        return schoolRepository.findByTeacherId(teacherId);
    }

    @Override
    public School createSchool(School school, String acceptLanguage) {
        List<ErrorMessage> errors = new ArrayList<>();
        Locale locale = getLocale(acceptLanguage);

        validateSchool(school, errors, locale);

        if (!errors.isEmpty()) {
            throw new SchoolValidationException(errors);
        }

        return schoolRepository.save(school);
    }

    @Override
    @Transactional
    public void softDeleteSchool(Integer schoolId, Integer teacherId, String acceptLanguage) {
        Locale locale = getLocale(acceptLanguage);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, locale)));

        if (!school.getTeacherId().equals(teacherId)) {
            throw new SchoolForbiddenException(messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, locale));
        }

        schoolRepository.softDeleteSchool(schoolId, teacherId);
    }

    @Override
    @Transactional
    public School updateSchool(Integer schoolId, School school, Integer teacherId, String acceptLanguage) {

        Locale locale = getLocale(acceptLanguage);
        List<ErrorMessage> errors = new ArrayList<>();
        validateSchool(school, errors, locale);

        if (!errors.isEmpty()) {
            throw new SchoolValidationException(errors);
        }

        School existingSchool = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, locale)));

        if (!existingSchool.getTeacherId().equals(teacherId)) {
            throw new SchoolForbiddenException(messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, locale));
        }

        existingSchool.setName(school.getName());
        existingSchool.setTown(school.getTown());
        existingSchool.setTlf(school.getTlf());

        return schoolRepository.save(existingSchool);
    }

    @Override
    public Optional<School> getSchoolById(Integer schoolId) {
        return schoolRepository.findById(schoolId);
    }

    private void validateSchool(School school, List<ErrorMessage> errors, Locale locale) {
        if (school.getName() == null || school.getName().trim().isEmpty()) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, locale);
            errors.add(new ErrorMessage("name", translatedMessage));
        }

        if (school.getTlf() != null && String.valueOf(school.getTlf()).length() != 9) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID, null, locale);
            errors.add(new ErrorMessage("tlf", translatedMessage));
        }
    }

    private Locale getLocale(String acceptLanguage) {
        if ("es".equalsIgnoreCase(acceptLanguage)) {
            return new Locale("es");
        } else {
            return Locale.ENGLISH;
        }
    }
}
