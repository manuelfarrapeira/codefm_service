package org.web.codefm.domain.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.web.codefm.domain.entity.teachernotebook.Class;

/**
 * Utility class for school year operations.
 * Provides methods to parse and manipulate school year formats.
 */
@Slf4j
@UtilityClass
public class SchoolYearUtil {

    /**
     * Parses a school year string to an integer for comparison purposes.
     * Converts format "YY/YY" to a numeric value (e.g., "24/25" to 2425).
     *
     * @param clazz The class entity containing the school year
     * @return The parsed school year as integer, or 0 if parsing fails
     */
    public static Integer parseSchoolYear(Class clazz) {
        try {
            String year = clazz.getSchoolYear().replace("/", "");
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            log.warn("Invalid schoolYear format: {}", clazz.getSchoolYear());
            return 0;
        }
    }
}

