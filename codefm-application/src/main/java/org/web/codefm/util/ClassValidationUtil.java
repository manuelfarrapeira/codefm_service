package org.web.codefm.util;

import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.service.teachernotebook.ClassService;
import org.web.codefm.domain.service.teachernotebook.SchoolService;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for common class validation operations.
 * Centralizes repeated validation logic across different services.
 */
@UtilityClass
public class ClassValidationUtil {

    /**
     * Validates and retrieves a class by its ID, ensuring it exists and belongs to a school owned by the specified teacher.
     *
     * @param classId       The ID of the class to validate
     * @param teacherId     The ID of the teacher who should own the school
     * @param classService  The class service to retrieve the class
     * @param schoolService The school service to validate school ownership
     * @param messageSource The message source for internationalization
     * @param locale        The locale for error messages
     * @return The validated class entity
     * @throws ClassNotFoundException  if the class doesn't exist
     * @throws ClassForbiddenException if the school doesn't belong to the teacher
     */
    public static Class validateClassOwnership(Integer classId, Integer teacherId, ClassService classService, SchoolService schoolService, MessageSource messageSource, Locale locale) {
        Class clazz = classService.getClassById(classId)
                .orElseThrow(() -> new ClassNotFoundException(
                        messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)));

        Optional<School> school = schoolService.getSchoolById(clazz.getSchoolId());

        if (school.isEmpty() || !school.get().getTeacherId().equals(teacherId)) {
            throw new ClassForbiddenException(
                    messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, locale));
        }

        return clazz;
    }
}

