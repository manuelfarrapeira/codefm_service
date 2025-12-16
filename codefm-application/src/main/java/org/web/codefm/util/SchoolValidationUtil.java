package org.web.codefm.util;

import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.service.teachernotebook.SchoolService;

import java.util.Locale;

/**
 * Utility class for common school validation operations.
 * Centralizes repeated validation logic across different services.
 */
@UtilityClass
public class SchoolValidationUtil {

    /**
     * Validates and retrieves a school by its ID, ensuring it exists and belongs to the specified teacher.
     *
     * @param schoolId      The ID of the school to validate
     * @param teacherId     The ID of the teacher who should own the school
     * @param schoolService The school service to retrieve the school
     * @param messageSource The message source for internationalization
     * @param locale        The locale for error messages
     * @return The validated school entity
     * @throws SchoolNotFoundException  if the school doesn't exist
     * @throws SchoolForbiddenException if the school doesn't belong to the teacher
     */
    public static School validateSchoolOwnership(Integer schoolId, Integer teacherId, SchoolService schoolService, MessageSource messageSource, Locale locale) {
        School school = schoolService.getSchoolById(schoolId)
                .orElseThrow(() -> new SchoolNotFoundException(
                        messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, locale)));

        if (!school.getTeacherId().equals(teacherId)) {
            throw new SchoolForbiddenException(
                    messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, locale));
        }

        return school;
    }
}

