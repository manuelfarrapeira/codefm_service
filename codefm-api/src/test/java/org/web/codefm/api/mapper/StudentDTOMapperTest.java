package org.web.codefm.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.model.StudentDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentDTOMapperTest {

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SchoolRepository schoolRepository;

    private StudentDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StudentDTOMapperImpl();
        ReflectionTestUtils.setField(mapper, "studentClassRepository", studentClassRepository);
        ReflectionTestUtils.setField(mapper, "classRepository", classRepository);
        ReflectionTestUtils.setField(mapper, "schoolRepository", schoolRepository);
    }

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_dto() {
            final Integer studentId = 1;
            final Student student = Student.builder()
                    .id(studentId)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .additionalInfo("Test info")
                    .photo("1.jpg")
                    .shape("SQUARE")
                    .classNumber(1)
                    .build();

            when(studentClassRepository.findClassIdsByStudentId(studentId)).thenReturn(Collections.emptyList());

            final StudentDTO result = mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getDateOfBirth()).isEqualTo("15/03/2010");
            assertThat(result.getAdditionalInfo()).isEqualTo("Test info");
            assertThat(result.getPhoto()).isEqualTo("1.jpg");
            assertThat(result.getShape()).isEqualTo("SQUARE");
            assertThat(result.getClassNumber()).isEqualTo(1);
            assertThat(result.getClassList()).isEmpty();
        }

        @Test
        void when_date_is_null_expect_null_date() {
            final Integer studentId = 2;
            final Student student = Student.builder()
                    .id(studentId)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(null)
                    .build();

            when(studentClassRepository.findClassIdsByStudentId(studentId)).thenReturn(Collections.emptyList());

            final StudentDTO result = mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getDateOfBirth()).isNull();
        }

        @Test
        void when_day_has_single_digit_expect_padded_date() {
            final Integer studentId = 3;
            final Student student = Student.builder()
                    .id(studentId)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 5))
                    .build();

            when(studentClassRepository.findClassIdsByStudentId(studentId)).thenReturn(Collections.emptyList());

            final StudentDTO result = mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isEqualTo("05/03/2010");
        }

        @Test
        void when_month_has_single_digit_expect_padded_date() {
            final Integer studentId = 4;
            final Student student = Student.builder()
                    .id(studentId)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 1, 15))
                    .build();

            when(studentClassRepository.findClassIdsByStudentId(studentId)).thenReturn(Collections.emptyList());

            final StudentDTO result = mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isEqualTo("15/01/2010");
        }

        @Test
        void when_optional_fields_are_null_expect_null_optional_values() {
            final Integer studentId = 5;
            final Student student = Student.builder()
                    .id(studentId)
                    .name("Juan")
                    .surnames("García López")
                    .additionalInfo(null)
                    .photo(null)
                    .build();

            when(studentClassRepository.findClassIdsByStudentId(studentId)).thenReturn(Collections.emptyList());

            final StudentDTO result = mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(5);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getAdditionalInfo()).isNull();
            assertThat(result.getPhoto()).isNull();
        }

        @Test
        void when_student_has_classes_expect_resolve_schools_and_classes() {
            final Integer studentId = 6;
            final Integer schoolId1 = 1;
            final Integer classId1 = 1;
            final Integer classId2 = 2;

            final Student student = Student.builder()
                    .id(studentId)
                    .name("Juan")
                    .surnames("García López")
                    .build();

            final Class class1 = Class.builder()
                    .id(classId1)
                    .schoolId(schoolId1)
                    .name("1A")
                    .build();

            final Class class2 = Class.builder()
                    .id(classId2)
                    .schoolId(schoolId1)
                    .name("1B")
                    .build();

            final School school1 = School.builder()
                    .id(schoolId1)
                    .name("School A")
                    .build();

            when(studentClassRepository.findClassIdsByStudentId(studentId))
                    .thenReturn(Arrays.asList(classId1, classId2));
            when(classRepository.findById(classId1)).thenReturn(Optional.of(class1));
            when(classRepository.findById(classId2)).thenReturn(Optional.of(class2));
            when(schoolRepository.findById(schoolId1)).thenReturn(Optional.of(school1));

            final StudentDTO result = mapper.toDTO(student);

            assertThat(result).isNotNull();
            assertThat(result.getClassList()).hasSize(1);
            assertThat(result.getClassList().get(0).getSchoolName()).isEqualTo("School A");
            assertThat(result.getClassList().get(0).getClasses()).hasSize(2);
            assertThat(result.getClassList().get(0).getClasses().get(0).getClassName()).isEqualTo("1A");
            assertThat(result.getClassList().get(0).getClasses().get(1).getClassName()).isEqualTo("1B");
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_list_has_students_expect_mapped_list() {
            final Integer studentId1 = 1;
            final Integer studentId2 = 2;
            
            final Student student1 = Student.builder()
                    .id(studentId1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .build();
            final Student student2 = Student.builder()
                    .id(studentId2)
                    .name("María")
                    .surnames("Pérez Sánchez")
                    .dateOfBirth(LocalDate.of(2011, 5, 20))
                    .build();
            final List<Student> students = Arrays.asList(student1, student2);

            when(studentClassRepository.findClassIdsByStudentId(studentId1)).thenReturn(Collections.emptyList());
            when(studentClassRepository.findClassIdsByStudentId(studentId2)).thenReturn(Collections.emptyList());

            final List<StudentDTO> result = mapper.toDTOList(students);

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
            final List<StudentDTO> result = mapper.toDTOList(List.of());

            assertThat(result).isEmpty();
        }
    }
}
