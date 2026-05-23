package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentClassJPARepository;
import org.web.codefm.infrastructure.mapper.StudentClassMapper;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassRepositoryImplTest {

    private StudentClassRepositoryImpl studentClassRepository;

    @Mock
    private StudentClassJPARepository studentClassJPARepository;

    @Mock
    private StudentClassMapper studentClassMapper;

    private final Integer classId = 10;
    private final Integer studentId = 20;

    @BeforeEach
    void beforeEach() {
        this.studentClassRepository = new StudentClassRepositoryImpl(this.studentClassJPARepository,
                this.studentClassMapper);
    }

    @Nested
    class FindByClassIdAndStudentId {

        @Test
        void when_association_exists_expect_student_class_returned() {
            final StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);
            final StudentClass studentClass = StudentClass.builder().id(1).classId(classId).studentId(studentId)
                    .build();
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findByClassIdAndStudentId(classId,
                    studentId)).thenReturn(Optional.of(entity));
            when(StudentClassRepositoryImplTest.this.studentClassMapper.toModel(entity)).thenReturn(studentClass);

            final Optional<StudentClass> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findByClassIdAndStudentId(classId, studentId);

            assertThat(result).isPresent();
            assertThat(result.get().getClassId()).isEqualTo(classId);
            assertThat(result.get().getStudentId()).isEqualTo(studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findByClassIdAndStudentId(classId,
                    studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper).toModel(entity);
        }

        @Test
        void when_association_does_not_exist_expect_empty_optional_returned() {
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findByClassIdAndStudentId(classId,
                    studentId)).thenReturn(Optional.empty());

            final Optional<StudentClass> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findByClassIdAndStudentId(classId, studentId);

            assertThat(result).isNotPresent();
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findByClassIdAndStudentId(classId,
                    studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper, never()).toModel(any());
        }
    }

    @Nested
    class Save {

        @Test
        void when_student_class_is_saved_expect_student_class_returned() {
            final StudentClass studentClass = StudentClass.builder().classId(classId).studentId(studentId).build();
            final StudentClassEntity entity = new StudentClassEntity(null, classId, studentId, null);
            final StudentClassEntity savedEntity = new StudentClassEntity(1, classId, studentId, null);
            final StudentClass savedStudentClass = StudentClass.builder().id(1).classId(classId).studentId(studentId)
                    .build();
            when(StudentClassRepositoryImplTest.this.studentClassMapper.toEntity(studentClass)).thenReturn(entity);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.save(entity)).thenReturn(savedEntity);
            when(StudentClassRepositoryImplTest.this.studentClassMapper.toModel(savedEntity))
                    .thenReturn(savedStudentClass);

            final StudentClass result = StudentClassRepositoryImplTest.this.studentClassRepository.save(studentClass);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getClassId()).isEqualTo(classId);
            assertThat(result.getStudentId()).isEqualTo(studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper).toEntity(studentClass);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).save(entity);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper).toModel(savedEntity);
        }
    }

    @Nested
    class Update {

        @Test
        void when_student_class_is_updated_expect_student_class_returned() {
            final StudentClass studentClass = StudentClass.builder().id(1).classId(classId).studentId(studentId)
                    .deletionDate(null).build();
            final StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);
            final StudentClassEntity updatedEntity = new StudentClassEntity(1, classId, studentId, null);
            final StudentClass updatedStudentClass = StudentClass.builder().id(1).classId(classId).studentId(studentId)
                    .deletionDate(null).build();
            when(StudentClassRepositoryImplTest.this.studentClassMapper.toEntity(studentClass)).thenReturn(entity);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.save(entity)).thenReturn(updatedEntity);
            when(StudentClassRepositoryImplTest.this.studentClassMapper.toModel(updatedEntity))
                    .thenReturn(updatedStudentClass);

            final StudentClass result = StudentClassRepositoryImplTest.this.studentClassRepository.update(studentClass);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getDeletionDate()).isNull();
            verify(StudentClassRepositoryImplTest.this.studentClassMapper).toEntity(studentClass);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).save(entity);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper).toModel(updatedEntity);
        }
    }

    @Nested
    class SoftDelete {

        @Test
        void when_association_exists_expect_deletion_date_set() {
            final StudentClassEntity entity = new StudentClassEntity(1, classId, studentId, null);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findByClassIdAndStudentId(classId,
                    studentId)).thenReturn(Optional.of(entity));
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.save(any(StudentClassEntity.class)))
                    .thenReturn(entity);

            StudentClassRepositoryImplTest.this.studentClassRepository.softDelete(classId, studentId);

            assertThat(entity.getDeletionDate()).isNotNull();
            assertThat(entity.getDeletionDate()).isEqualTo(LocalDate.now());
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findByClassIdAndStudentId(classId,
                    studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).save(entity);
        }

        @Test
        void when_association_does_not_exist_expect_exception_thrown() {
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findByClassIdAndStudentId(classId,
                    studentId)).thenReturn(Optional.empty());
            final ThrowingCallable callable = () -> StudentClassRepositoryImplTest.this.studentClassRepository
                    .softDelete(classId, studentId);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findByClassIdAndStudentId(classId,
                    studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository, never()).save(any());
        }
    }

    @Nested
    class FindClassIdsByStudentId {

        @Test
        void when_classes_exist_expect_class_ids_returned() {
            final Integer requestedStudentId = 1;
            final List<Integer> expectedClassIds = Arrays.asList(10, 20, 30);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository
                    .findClassIdsByStudentIdAndDeletionDateIsNull(requestedStudentId)).thenReturn(expectedClassIds);

            final List<Integer> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findClassIdsByStudentId(requestedStudentId);

            assertThat(result).isNotNull().hasSize(3).isEqualTo(expectedClassIds);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository)
                    .findClassIdsByStudentIdAndDeletionDateIsNull(requestedStudentId);
        }

        @Test
        void when_no_classes_exist_expect_empty_list_returned() {
            final Integer requestedStudentId = 1;
            final List<Integer> emptyList = Collections.emptyList();
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository
                    .findClassIdsByStudentIdAndDeletionDateIsNull(requestedStudentId)).thenReturn(emptyList);

            final List<Integer> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findClassIdsByStudentId(requestedStudentId);

            assertThat(result).isNotNull().isEmpty();
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository)
                    .findClassIdsByStudentIdAndDeletionDateIsNull(requestedStudentId);
        }
    }

    @Nested
    class FindClassIdsByTeacherId {

        @Test
        void when_associations_exist_expect_student_to_class_map_returned() {
            final Integer teacherId = 1;
            final StudentClassEntity entity1 = new StudentClassEntity(1, 10, 100, null);
            final StudentClassEntity entity2 = new StudentClassEntity(2, 20, 100, null);
            final StudentClassEntity entity3 = new StudentClassEntity(3, 30, 200, null);
            final StudentClassEntity entity4 = new StudentClassEntity(4, 40, 200, null);
            final List<StudentClassEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId)).thenReturn(entities);

            final Map<Integer, List<Integer>> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findClassIdsByTeacherId(teacherId);

            assertThat(result).isNotNull().hasSize(2).containsKeys(100, 200);
            assertThat(result.get(100)).isEqualTo(Arrays.asList(10, 20));
            assertThat(result.get(200)).isEqualTo(Arrays.asList(30, 40));
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository)
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId);
        }

        @Test
        void when_no_associations_exist_expect_empty_map_returned() {
            final Integer teacherId = 1;
            final List<StudentClassEntity> emptyList = Collections.emptyList();
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId)).thenReturn(emptyList);

            final Map<Integer, List<Integer>> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findClassIdsByTeacherId(teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository)
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId);
        }

        @Test
        void when_multiple_classes_belong_to_same_student_expect_grouped_class_ids_returned() {
            final Integer teacherId = 1;
            final Integer requestedStudentId = 100;
            final StudentClassEntity entity1 = new StudentClassEntity(1, 10, requestedStudentId, null);
            final StudentClassEntity entity2 = new StudentClassEntity(2, 20, requestedStudentId, null);
            final StudentClassEntity entity3 = new StudentClassEntity(3, 30, requestedStudentId, null);
            final List<StudentClassEntity> entities = Arrays.asList(entity1, entity2, entity3);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId)).thenReturn(entities);

            final Map<Integer, List<Integer>> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findClassIdsByTeacherId(teacherId);

            assertThat(result).isNotNull().hasSize(1).containsKey(requestedStudentId);
            assertThat(result.get(requestedStudentId)).hasSize(3).isEqualTo(Arrays.asList(10, 20, 30));
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository)
                    .findAllByTeacherIdAndDeletionDateIsNull(teacherId);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_student_class_exists_expect_student_class_returned() {
            final Integer id = 1;
            final StudentClassEntity entity = new StudentClassEntity(id, classId, studentId, null);
            final StudentClass studentClass = StudentClass.builder().id(id).classId(classId).studentId(studentId)
                    .build();
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findById(id))
                    .thenReturn(Optional.of(entity));
            when(StudentClassRepositoryImplTest.this.studentClassMapper.toModel(entity)).thenReturn(studentClass);

            final Optional<StudentClass> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
            assertThat(result.get().getClassId()).isEqualTo(classId);
            assertThat(result.get().getStudentId()).isEqualTo(studentId);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findById(id);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper).toModel(entity);
        }

        @Test
        void when_student_class_does_not_exist_expect_empty_optional_returned() {
            final Integer id = 999;
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findById(id)).thenReturn(Optional.empty());

            final Optional<StudentClass> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findById(id);

            assertThat(result).isNotPresent();
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findById(id);
            verify(StudentClassRepositoryImplTest.this.studentClassMapper, never()).toModel(any());
        }
    }

    @Nested
    class SoftDeleteByClassId {

        @Test
        void when_associations_are_soft_deleted_expect_delete_delegated() {
            StudentClassRepositoryImplTest.this.studentClassRepository.softDeleteByClassId(classId);

            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).softDeleteByClassId(classId);
        }
    }

    @Nested
    class SoftDeleteByStudentId {

        @Test
        void when_associations_are_soft_deleted_expect_delete_delegated() {
            StudentClassRepositoryImplTest.this.studentClassRepository.softDeleteByStudentId(studentId);

            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).softDeleteByStudentId(studentId);
        }
    }

    @Nested
    class FindActiveStudentIdsByClassId {

        @Test
        void when_students_exist_expect_student_ids_returned() {
            final List<Integer> expected = List.of(1, 2, 3);
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findActiveStudentIdsByClassId(classId))
                    .thenReturn(expected);

            final List<Integer> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findActiveStudentIdsByClassId(classId);

            assertThat(result).isEqualTo(expected);
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findActiveStudentIdsByClassId(classId);
        }

        @Test
        void when_no_students_exist_expect_empty_list_returned() {
            when(StudentClassRepositoryImplTest.this.studentClassJPARepository.findActiveStudentIdsByClassId(classId))
                    .thenReturn(List.of());

            final List<Integer> result = StudentClassRepositoryImplTest.this.studentClassRepository
                    .findActiveStudentIdsByClassId(classId);

            assertThat(result).isEmpty();
            verify(StudentClassRepositoryImplTest.this.studentClassJPARepository).findActiveStudentIdsByClassId(classId);
        }
    }
}
