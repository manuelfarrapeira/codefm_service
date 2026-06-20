package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.mapper.StudentMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRepositoryImplTest {

    private StudentRepositoryImpl studentRepository;

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.studentRepository = new StudentRepositoryImpl(this.studentJPARepository, this.studentMapper,
                this.cacheEvictionService);
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_entity_saved() {
            final Student studentToSave = Student.builder()
                    .teacherId(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .build();
            final StudentEntity studentEntity = new StudentEntity();
            final StudentEntity savedStudentEntity = new StudentEntity();
            savedStudentEntity.setId(1);
            savedStudentEntity.setTeacherId(1);
            savedStudentEntity.setName("Juan");
            savedStudentEntity.setSurnames("García López");
            final Student savedStudent = Student.builder()
                    .id(1)
                    .teacherId(1)
                    .name("Juan")
                    .surnames("García López")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .build();

            when(StudentRepositoryImplTest.this.studentMapper.toEntity(studentToSave)).thenReturn(studentEntity);
            when(StudentRepositoryImplTest.this.studentJPARepository.save(studentEntity)).thenReturn(savedStudentEntity);
            when(StudentRepositoryImplTest.this.studentMapper.toModel(savedStudentEntity)).thenReturn(savedStudent);

            final Student result = StudentRepositoryImplTest.this.studentRepository.save(studentToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTeacherId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toEntity(studentToSave);
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1)).save(studentEntity);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModel(savedStudentEntity);
            verify(StudentRepositoryImplTest.this.cacheEvictionService).evictByTeacher("studentsByTeacher");
        }
    }

    @Nested
    class FindByIdAndTeacherIdAndDeletionDateIsNull {

        @Test
        void when_student_found_expect_student_returned() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(studentId);
            studentEntity.setTeacherId(teacherId);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García López");
            studentEntity.setDeletionDate(null);
            final Student expectedStudent = Student.builder()
                    .id(studentId)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .build();

            when(StudentRepositoryImplTest.this.studentJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)).thenReturn(Optional.of(studentEntity));
            when(StudentRepositoryImplTest.this.studentMapper.toModel(studentEntity)).thenReturn(expectedStudent);

            final Optional<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);

            assertThat(result).isPresent().contains(expectedStudent);
            assertThat(result.orElseThrow().getName()).isEqualTo("Juan");
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModel(studentEntity);
        }

        @Test
        void when_student_not_found_expect_empty_optional() {
            final Integer studentId = 999;
            final Integer teacherId = 1;

            when(StudentRepositoryImplTest.this.studentJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)).thenReturn(Optional.empty());

            final Optional<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);

            assertThat(result).isNotPresent();
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
            verify(StudentRepositoryImplTest.this.studentMapper, never()).toModel(any(StudentEntity.class));
        }
    }

    @Nested
    class Update {

        @Test
        void when_valid_data_expect_entity_updated() {
            final Student studentToUpdate = Student.builder()
                    .id(1)
                    .name("Juan Carlos")
                    .surnames("García López Pérez")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .additionalInfo("Updated info")
                    .build();
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(1);
            final StudentEntity updatedStudentEntity = new StudentEntity();
            updatedStudentEntity.setId(1);
            updatedStudentEntity.setName("Juan Carlos");
            updatedStudentEntity.setSurnames("García López Pérez");
            final Student updatedStudent = Student.builder()
                    .id(1)
                    .name("Juan Carlos")
                    .surnames("García López Pérez")
                    .dateOfBirth(LocalDate.of(2010, 3, 15))
                    .additionalInfo("Updated info")
                    .build();

            when(StudentRepositoryImplTest.this.studentMapper.toEntity(studentToUpdate)).thenReturn(studentEntity);
            when(StudentRepositoryImplTest.this.studentJPARepository.save(studentEntity)).thenReturn(updatedStudentEntity);
            when(StudentRepositoryImplTest.this.studentMapper.toModel(updatedStudentEntity)).thenReturn(updatedStudent);

            final Student result = StudentRepositoryImplTest.this.studentRepository.update(studentToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Juan Carlos");
            assertThat(result.getSurnames()).isEqualTo("García López Pérez");
            assertThat(result.getAdditionalInfo()).isEqualTo("Updated info");
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toEntity(studentToUpdate);
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1)).save(studentEntity);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModel(updatedStudentEntity);
            verify(StudentRepositoryImplTest.this.cacheEvictionService).evictByTeacher("studentsByTeacher");
        }
    }

    @Nested
    class SoftDelete {

        @Test
        void when_active_student_expect_student_soft_deleted() {
            final Integer studentId = 1;
            final Integer teacherId = 1;
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(studentId);
            studentEntity.setTeacherId(teacherId);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García López");
            studentEntity.setDeletionDate(null);
            final Student deletedStudent = Student.builder()
                    .id(studentId)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .deletionDate(LocalDate.now())
                    .build();

            when(StudentRepositoryImplTest.this.studentJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)).thenReturn(Optional.of(studentEntity));
            when(StudentRepositoryImplTest.this.studentJPARepository.save(any(StudentEntity.class))).thenReturn(studentEntity);
            when(StudentRepositoryImplTest.this.studentMapper.toModel(any(StudentEntity.class))).thenReturn(deletedStudent);

            final Student result = StudentRepositoryImplTest.this.studentRepository.softDelete(studentId, teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(studentId);
            assertThat(result.getDeletionDate()).isNotNull();
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1)).save(any(StudentEntity.class));
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModel(any(StudentEntity.class));
            verify(StudentRepositoryImplTest.this.cacheEvictionService).evictByTeacher("studentsByTeacher");
        }

        @Test
        void when_student_not_found_expect_exception() {
            final Integer studentId = 999;
            final Integer teacherId = 1;

            when(StudentRepositoryImplTest.this.studentJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)).thenReturn(Optional.empty());

            final ThrowingCallable callable = () -> StudentRepositoryImplTest.this.studentRepository
                    .softDelete(studentId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
            verify(StudentRepositoryImplTest.this.studentJPARepository, never()).save(any(StudentEntity.class));
            verify(StudentRepositoryImplTest.this.studentMapper, never()).toModel(any(StudentEntity.class));
        }

        @Test
        void when_student_already_deleted_expect_exception() {
            final Integer studentId = 1;
            final Integer teacherId = 1;

            when(StudentRepositoryImplTest.this.studentJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)).thenReturn(Optional.empty());

            final ThrowingCallable callable = () -> StudentRepositoryImplTest.this.studentRepository
                    .softDelete(studentId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
            verify(StudentRepositoryImplTest.this.studentJPARepository, never()).save(any(StudentEntity.class));
        }
    }

    @Nested
    class SearchStudents {

        @Test
        void when_filtered_by_id_expect_matching_students() {
            final Integer teacherId = 1;
            final Integer studentId = 1;
            final StudentEntity studentEntity1 = new StudentEntity();
            studentEntity1.setId(1);
            studentEntity1.setTeacherId(teacherId);
            studentEntity1.setName("Juan");
            studentEntity1.setSurnames("García López");
            final Student student1 = Student.builder()
                    .id(1)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .build();
            final List<StudentEntity> entities = List.of(studentEntity1);
            final List<Student> expectedStudents = List.of(student1);

            when(StudentRepositoryImplTest.this.studentJPARepository.searchStudents(teacherId, studentId, null, null))
                    .thenReturn(entities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(entities)).thenReturn(expectedStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .searchStudents(teacherId, studentId, null, null);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .searchStudents(teacherId, studentId, null, null);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_filtered_by_name_expect_matching_students() {
            final Integer teacherId = 1;
            final String name = "Juan";
            final StudentEntity studentEntity1 = new StudentEntity();
            studentEntity1.setId(1);
            studentEntity1.setTeacherId(teacherId);
            studentEntity1.setName("Juan");
            studentEntity1.setSurnames("García López");
            final StudentEntity studentEntity2 = new StudentEntity();
            studentEntity2.setId(2);
            studentEntity2.setTeacherId(teacherId);
            studentEntity2.setName("Juan Carlos");
            studentEntity2.setSurnames("Pérez Martín");
            final Student student1 = Student.builder()
                    .id(1)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .build();
            final Student student2 = Student.builder()
                    .id(2)
                    .teacherId(teacherId)
                    .name("Juan Carlos")
                    .surnames("Pérez Martín")
                    .build();
            final List<StudentEntity> entities = Arrays.asList(studentEntity1, studentEntity2);
            final List<Student> expectedStudents = Arrays.asList(student1, student2);

            when(StudentRepositoryImplTest.this.studentJPARepository.searchStudents(teacherId, null, name, null))
                    .thenReturn(entities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(entities)).thenReturn(expectedStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .searchStudents(teacherId, null, name, null);

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(1).getName()).isEqualTo("Juan Carlos");
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1)).searchStudents(teacherId, null, name, null);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_filtered_by_surnames_expect_matching_students() {
            final Integer teacherId = 1;
            final String surnames = "García";
            final StudentEntity studentEntity1 = new StudentEntity();
            studentEntity1.setId(1);
            studentEntity1.setTeacherId(teacherId);
            studentEntity1.setName("Juan");
            studentEntity1.setSurnames("García López");
            final Student student1 = Student.builder()
                    .id(1)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .build();
            final List<StudentEntity> entities = List.of(studentEntity1);
            final List<Student> expectedStudents = List.of(student1);

            when(StudentRepositoryImplTest.this.studentJPARepository.searchStudents(teacherId, null, null, surnames))
                    .thenReturn(entities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(entities)).thenReturn(expectedStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .searchStudents(teacherId, null, null, surnames);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getSurnames()).isEqualTo("García López");
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .searchStudents(teacherId, null, null, surnames);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_filtered_by_multiple_fields_expect_matching_students() {
            final Integer teacherId = 1;
            final String name = "Juan";
            final String surnames = "García";
            final StudentEntity studentEntity1 = new StudentEntity();
            studentEntity1.setId(1);
            studentEntity1.setTeacherId(teacherId);
            studentEntity1.setName("Juan");
            studentEntity1.setSurnames("García López");
            final Student student1 = Student.builder()
                    .id(1)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .build();
            final List<StudentEntity> entities = List.of(studentEntity1);
            final List<Student> expectedStudents = List.of(student1);

            when(StudentRepositoryImplTest.this.studentJPARepository.searchStudents(teacherId, null, name, surnames))
                    .thenReturn(entities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(entities)).thenReturn(expectedStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .searchStudents(teacherId, null, name, surnames);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(0).getSurnames()).isEqualTo("García López");
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1)).searchStudents(teacherId, null, name, surnames);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_no_students_found_expect_empty_list() {
            final Integer teacherId = 1;
            final Integer studentId = 999;
            final List<StudentEntity> emptyEntities = List.of();
            final List<Student> emptyStudents = List.of();

            when(StudentRepositoryImplTest.this.studentJPARepository.searchStudents(teacherId, studentId, null, null))
                    .thenReturn(emptyEntities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(emptyEntities)).thenReturn(emptyStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .searchStudents(teacherId, studentId, null, null);

            assertThat(result).isNotNull().isEmpty();
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .searchStudents(teacherId, studentId, null, null);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(emptyEntities);
        }
    }

    @Nested
    class FindAllByTeacherId {

        @Test
        void when_students_exist_expect_students_returned() {
            final Integer teacherId = 1;
            final StudentEntity studentEntity1 = new StudentEntity();
            studentEntity1.setId(1);
            studentEntity1.setTeacherId(teacherId);
            studentEntity1.setName("Juan");
            studentEntity1.setSurnames("García López");
            studentEntity1.setDeletionDate(null);
            final StudentEntity studentEntity2 = new StudentEntity();
            studentEntity2.setId(2);
            studentEntity2.setTeacherId(teacherId);
            studentEntity2.setName("María");
            studentEntity2.setSurnames("Pérez Martínez");
            studentEntity2.setDeletionDate(null);
            final Student student1 = Student.builder()
                    .id(1)
                    .teacherId(teacherId)
                    .name("Juan")
                    .surnames("García López")
                    .build();
            final Student student2 = Student.builder()
                    .id(2)
                    .teacherId(teacherId)
                    .name("María")
                    .surnames("Pérez Martínez")
                    .build();
            final List<StudentEntity> entities = Arrays.asList(studentEntity1, studentEntity2);
            final List<Student> expectedStudents = Arrays.asList(student1, student2);

            when(StudentRepositoryImplTest.this.studentJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId))
                    .thenReturn(entities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(entities)).thenReturn(expectedStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository.findAllByTeacherId(teacherId);

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(1).getName()).isEqualTo("María");
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_no_students_exist_expect_empty_list() {
            final Integer teacherId = 999;
            final List<StudentEntity> emptyEntities = List.of();
            final List<Student> emptyStudents = List.of();

            when(StudentRepositoryImplTest.this.studentJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId))
                    .thenReturn(emptyEntities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(emptyEntities)).thenReturn(emptyStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository.findAllByTeacherId(teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(StudentRepositoryImplTest.this.studentJPARepository, times(1))
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId);
            verify(StudentRepositoryImplTest.this.studentMapper, times(1)).toModelList(emptyEntities);
        }
    }

    @Nested
    class FindByIdsAndTeacherIdAndDeletionDateIsNull {

        @Test
        void when_matching_students_exist_expect_students_returned() {
            final Integer teacherId = 1;
            final List<Integer> ids = List.of(1, 2);
            final StudentEntity entity1 = new StudentEntity();
            entity1.setId(1);
            entity1.setTeacherId(teacherId);
            entity1.setName("Juan");
            entity1.setSurnames("García");
            final StudentEntity entity2 = new StudentEntity();
            entity2.setId(2);
            entity2.setTeacherId(teacherId);
            entity2.setName("Ana");
            entity2.setSurnames("López");
            final List<StudentEntity> entities = List.of(entity1, entity2);
            final List<Student> expected = List.of(
                    Student.builder().id(1).teacherId(teacherId).name("Juan").surnames("García").build(),
                    Student.builder().id(2).teacherId(teacherId).name("Ana").surnames("López").build());

            when(StudentRepositoryImplTest.this.studentJPARepository.findByIdInAndTeacherIdAndDeletionDateIsNull(ids, teacherId))
                    .thenReturn(entities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(entities)).thenReturn(expected);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .findByIdsAndTeacherIdAndDeletionDateIsNull(ids, teacherId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Juan");
            assertThat(result.get(1).getName()).isEqualTo("Ana");
            verify(StudentRepositoryImplTest.this.studentJPARepository).findByIdInAndTeacherIdAndDeletionDateIsNull(ids,
                    teacherId);
            verify(StudentRepositoryImplTest.this.studentMapper).toModelList(entities);
        }

        @Test
        void when_no_matching_students_expect_empty_list() {
            final Integer teacherId = 1;
            final List<Integer> ids = List.of(999);
            final List<StudentEntity> emptyEntities = List.of();
            final List<Student> emptyStudents = List.of();

            when(StudentRepositoryImplTest.this.studentJPARepository.findByIdInAndTeacherIdAndDeletionDateIsNull(ids,
                    teacherId)).thenReturn(emptyEntities);
            when(StudentRepositoryImplTest.this.studentMapper.toModelList(emptyEntities)).thenReturn(emptyStudents);

            final List<Student> result = StudentRepositoryImplTest.this.studentRepository
                    .findByIdsAndTeacherIdAndDeletionDateIsNull(ids, teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(StudentRepositoryImplTest.this.studentJPARepository).findByIdInAndTeacherIdAndDeletionDateIsNull(ids,
                    teacherId);
            verify(StudentRepositoryImplTest.this.studentMapper).toModelList(emptyEntities);
        }
    }
}
