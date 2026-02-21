package org.web.codefm.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.web.codefm.domain.entity.teachernotebook.Class;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchoolYearUtilTest {

    @ParameterizedTest
    @CsvSource({"24/25, 2425", "2425, 2425", "invalid, 0"})
    void parseSchoolYear_shouldReturnExpectedValue(String schoolYear, int expectedResult) {
        // Given
        Class clazz = Class.builder().schoolYear(schoolYear).build();

        // When
        Integer result = SchoolYearUtil.parseSchoolYear(clazz);

        // Then
        assertEquals(expectedResult, result);
    }

    @Test
    void parseSchoolYear_shouldHandleDifferentYearFormats() {
        // Given
        Class clazz1 = Class.builder().schoolYear("22/23").build();
        Class clazz2 = Class.builder().schoolYear("23/24").build();
        Class clazz3 = Class.builder().schoolYear("24/25").build();

        // When
        Integer result1 = SchoolYearUtil.parseSchoolYear(clazz1);
        Integer result2 = SchoolYearUtil.parseSchoolYear(clazz2);
        Integer result3 = SchoolYearUtil.parseSchoolYear(clazz3);

        // Then
        assertEquals(2223, result1);
        assertEquals(2324, result2);
        assertEquals(2425, result3);
        assertTrue(result3 > result2);
        assertTrue(result2 > result1);
    }
}

