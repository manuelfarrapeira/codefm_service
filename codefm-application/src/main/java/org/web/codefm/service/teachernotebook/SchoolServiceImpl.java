package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.domain.service.teachernotebook.SchoolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        Locale locale;
        if ("es".equalsIgnoreCase(acceptLanguage)) {
            locale = new Locale("es");
        } else {
            locale = Locale.ENGLISH;
        }

        if (school.getName() == null || school.getName().trim().isEmpty()) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, locale);
            errors.add(new ErrorMessage("name", translatedMessage));
        }

        if (school.getTlf() != null && String.valueOf(school.getTlf()).length() != 9) {
            String translatedMessage = messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID, null, locale);
            errors.add(new ErrorMessage("tlf", translatedMessage));
        }

        if (!errors.isEmpty()) {
            throw new SchoolValidationException(errors);
        }

        return schoolRepository.save(school);
    }
}
