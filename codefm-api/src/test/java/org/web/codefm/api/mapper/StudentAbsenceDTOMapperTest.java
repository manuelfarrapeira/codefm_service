package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.model.StudentAbsenceDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentAbsenceDTOMapperTest {

    private final StudentAbsenceDTOMapper mapper = new StudentAbsenceDTOMapperImpl();

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_dto() {
            final StudentAbsence absence = StudentAbsence.builder().id(1).studentId(10).studentName("Juan")
                    .studentSurnames("García López").classId(5).subjectId(3).subjectName("Matemáticas")
                    .absenceDate(LocalDate.of(2026, 3, 15)).build();

            final StudentAbsenceDTO result = StudentAbsenceDTOMapperTest.this.mapper.toDTO(absence);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getStudentId()).isEqualTo(10);
            assertThat(result.getStudentName()).isEqualTo("Juan");
            assertThat(result.getStudentSurnames()).isEqualTo("García López");
            assertThat(result.getClassId()).isEqualTo(5);
            assertThat(result.getSubjectId()).isEqualTo(3);
            assertThat(result.getSubjectName()).isEqualTo("Matemáticas");
            assertThat(result.getAbsenceDate()).isEqualTo("15/03/2026");
        }

        @Test
        void when_date_is_null_expect_null_absence_date() {
            final StudentAbsence absence = StudentAbsence.builder().id(1).studentId(10).absenceDate(null).build();

            final StudentAbsenceDTO result = StudentAbsenceDTOMapperTest.this.mapper.toDTO(absence);

            assertThat(result).isNotNull();
            assertThat(result.getAbsenceDate()).isNull();
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_input_has_elements_expect_mapped_list() {
            final List<StudentAbsence> absences = Arrays.asList(
                    StudentAbsence.builder().id(1).absenceDate(LocalDate.of(2026, 3, 15)).build(),
                    StudentAbsence.builder().id(2).absenceDate(LocalDate.of(2026, 3, 16)).build());

            final List<StudentAbsenceDTO> result = StudentAbsenceDTOMapperTest.this.mapper.toDTOList(absences);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAbsenceDate()).isEqualTo("15/03/2026");
            assertThat(result.get(1).getAbsenceDate()).isEqualTo("16/03/2026");
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(StudentAbsenceDTOMapperTest.this.mapper.toDTOList(Collections.emptyList())).isEmpty();
        }
    }
}
