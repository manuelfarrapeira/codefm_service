package org.web.codefm.domain.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.web.codefm.domain.entity.teachernotebook.Class;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolYearUtilTest {

    @Nested
    class ParseSchoolYear {

        @ParameterizedTest
        @CsvSource({"24/25, 2425", "2425, 2425", "invalid, 0"})
        void when_school_year_is_parsed_expect_expected_value(final String schoolYear, final int expectedResult) {
            final Class clazz = Class.builder().schoolYear(schoolYear).build();

            final Integer result = SchoolYearUtil.parseSchoolYear(clazz);

            assertThat(result).isEqualTo(expectedResult);
        }

        @ParameterizedTest
        @CsvSource({"22/23, 23/24", "23/24, 24/25"})
        void when_school_years_are_compared_expect_ascending_values(final String lowerSchoolYear,
                                                                    final String higherSchoolYear) {
            final Class lowerClass = Class.builder().schoolYear(lowerSchoolYear).build();
            final Class higherClass = Class.builder().schoolYear(higherSchoolYear).build();

            final Integer lowerResult = SchoolYearUtil.parseSchoolYear(lowerClass);
            final Integer higherResult = SchoolYearUtil.parseSchoolYear(higherClass);

            assertThat(higherResult).isGreaterThan(lowerResult);
        }
    }
}
