package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.model.StudentDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentDTOMapperTest {

    private final StudentDTOMapper mapper = new StudentDTOMapperImpl();

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_dto() {
            final Student student = Student.builder()
                    .id(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .additionalInfo("Test info")
                    .photo("1.jpg")
                    .shape("SQUARE")
                    .build();

            final StudentDTO result = StudentDTOMapperTest.this.mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getDateOfBirth()).isEqualTo("15/03/2010");
            assertThat(result.getAdditionalInfo()).isEqualTo("Test info");
            assertThat(result.getPhoto()).isEqualTo("1.jpg");
            assertThat(result.getShape()).isEqualTo("SQUARE");
        }

        @Test
        void when_date_is_null_expect_null_date() {
            final Student student = Student.builder()
                    .id(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(null)
                    .build();

            final StudentDTO result = StudentDTOMapperTest.this.mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getDateOfBirth()).isNull();
        }

        @Test
        void when_day_has_single_digit_expect_padded_date() {
            final Student student = Student.builder()
                    .id(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 5))
                    .build();

            final StudentDTO result = StudentDTOMapperTest.this.mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isEqualTo("05/03/2010");
        }

        @Test
        void when_month_has_single_digit_expect_padded_date() {
            final Student student = Student.builder()
                    .id(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 1, 15))
                    .build();

            final StudentDTO result = StudentDTOMapperTest.this.mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isEqualTo("15/01/2010");
        }

        @Test
        void when_optional_fields_are_null_expect_null_optional_values() {
            final Student student = Student.builder()
                    .id(1)
                    .name("Juan")
                    .surnames("García López")
                    .additionalInfo(null)
                    .photo(null)
                    .build();

            final StudentDTO result = StudentDTOMapperTest.this.mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getAdditionalInfo()).isNull();
            assertThat(result.getPhoto()).isNull();
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_list_has_students_expect_mapped_list() {
            final Student student1 = Student.builder()
                    .id(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .build();
            final Student student2 = Student.builder()
                    .id(2)
                    .name("María")
                    .surnames("Pérez Sánchez")
                    .dateOfBirth(LocalDate.of(2011, 5, 20))
                    .build();
            final List<Student> students = Arrays.asList(student1, student2);

            final List<StudentDTO> result = StudentDTOMapperTest.this.mapper.toDTOList(students);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(0).getDateOfBirth()).isEqualTo("15/03/2010");
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getName()).isEqualTo("María");
            assertThat(result.get(1).getDateOfBirth()).isEqualTo("20/05/2011");
        }

        @Test
        void when_list_is_empty_expect_empty_list() {
            final List<StudentDTO> result = StudentDTOMapperTest.this.mapper.toDTOList(List.of());

            assertThat(result).isEmpty();
        }
    }
}
