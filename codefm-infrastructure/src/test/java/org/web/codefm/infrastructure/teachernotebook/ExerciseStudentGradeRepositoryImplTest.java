package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.*;
import org.web.codefm.infrastructure.jpa.teachernotebook.*;
import org.web.codefm.infrastructure.mapper.ExerciseStudentDocumentMapper;
import org.web.codefm.infrastructure.mapper.ExerciseStudentGradeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentGradeRepositoryImplTest {

    private ExerciseStudentGradeRepositoryImpl exerciseStudentGradeRepository;

    @Mock
    private ExerciseStudentGradeJPARepository exerciseStudentGradeJPARepository;

    @Mock
    private ExerciseJPARepository exerciseJPARepository;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private ExerciseStudentDocumentJPARepository exerciseStudentDocumentJPARepository;

    @Mock
    private ExerciseStudentGradeMapper exerciseStudentGradeMapper;

    @Mock
    private ExerciseStudentDocumentMapper exerciseStudentDocumentMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.exerciseStudentGradeRepository = new ExerciseStudentGradeRepositoryImpl(
                this.exerciseStudentGradeJPARepository,
                this.exerciseJPARepository,
                this.subjectClassJPARepository,
                this.subjectJPARepository,
                this.studentJPARepository,
                this.exerciseStudentDocumentJPARepository,
                this.exerciseStudentGradeMapper,
                this.exerciseStudentDocumentMapper,
                this.cacheEvictionService
        );
    }

    @Nested
    class FindByClassId {

        @Test
        void when_data_exists_expect_enriched_grades_returned() {
            final SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
            final ExerciseStudentGradeEntity gradeEntity = new ExerciseStudentGradeEntity(1, 5, 100, 8.0, "Good", null);
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8.0).build();
            final ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
            final StudentEntity studentEntity = new StudentEntity();

            studentEntity.setId(5);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García");

            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(List.of(scEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findActiveIdsBySubjectClassIds(List.of(10))).thenReturn(List.of(100));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.findByExerciseIdInAndDeletionDateIsNull(List.of(100))).thenReturn(List.of(gradeEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeMapper.toModelList(List.of(gradeEntity))).thenReturn(new ArrayList<>(List.of(grade)));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findAllById(List.of(100))).thenReturn(List.of(exerciseEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findAllById(List.of(10))).thenReturn(List.of(scEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectJPARepository.findAllById(List.of(1))).thenReturn(List.of(subjectEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.studentJPARepository.findAllById(List.of(5))).thenReturn(List.of(studentEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByGradeIdIn(List.of(1))).thenReturn(List.of());
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(List.of())).thenReturn(List.of());

            final List<ExerciseStudentGrade> result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.findByClassId(1);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getSubjectName()).isEqualTo("Math");
            assertThat(result.get(0).getStudentName()).isEqualTo("Juan");
        }

        @Test
        void when_no_subject_classes_expect_empty_list_returned() {
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(new ArrayList<>());

            final List<ExerciseStudentGrade> result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.findByClassId(1);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class FindByClassIdAndStudentId {

        @Test
        void when_data_exists_expect_grades_returned() {
            final SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
            final ExerciseStudentGradeEntity gradeEntity = new ExerciseStudentGradeEntity(1, 5, 100, 8.0, "Good", null);
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8.0).build();
            final ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
            final StudentEntity studentEntity = new StudentEntity();

            studentEntity.setId(5);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García");

            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(List.of(scEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findActiveIdsBySubjectClassIds(List.of(10))).thenReturn(List.of(100));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.findByExerciseIdInAndStudentIdAndDeletionDateIsNull(List.of(100), 5)).thenReturn(List.of(gradeEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeMapper.toModelList(List.of(gradeEntity))).thenReturn(new ArrayList<>(List.of(grade)));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findAllById(List.of(100))).thenReturn(List.of(exerciseEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findAllById(List.of(10))).thenReturn(List.of(scEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectJPARepository.findAllById(List.of(1))).thenReturn(List.of(subjectEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.studentJPARepository.findAllById(List.of(5))).thenReturn(List.of(studentEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByGradeIdIn(List.of(1))).thenReturn(List.of());
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(List.of())).thenReturn(List.of());

            final List<ExerciseStudentGrade> result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.findByClassIdAndStudentId(1, 5);

            assertThat(result).isNotNull().hasSize(1);
        }

        @Test
        void when_no_exercises_expect_empty_list_returned() {
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(1)).thenReturn(new ArrayList<>());

            final List<ExerciseStudentGrade> result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.findByClassIdAndStudentId(1, 5);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_grade_found_expect_enriched_grade_returned() {
            final ExerciseStudentGradeEntity entity = new ExerciseStudentGradeEntity(1, 5, 100, 8.0, "Good", null);
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8.0).build();
            final ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
            final SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
            final StudentEntity studentEntity = new StudentEntity();

            studentEntity.setId(5);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García");

            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.findByIdAndTeacherId(1, 1)).thenReturn(Optional.of(entity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeMapper.toModel(entity)).thenReturn(grade);
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findById(100)).thenReturn(Optional.of(exerciseEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findById(10)).thenReturn(Optional.of(scEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.studentJPARepository.findById(5)).thenReturn(Optional.of(studentEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByGradeId(1)).thenReturn(List.of());
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(List.of())).thenReturn(List.of());

            final Optional<ExerciseStudentGrade> result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(1, 1);

            assertThat(result).isPresent();
            assertThat(result.orElseThrow().getSubjectName()).isEqualTo("Math");
            assertThat(result.orElseThrow().getStudentName()).isEqualTo("Juan");
        }

        @Test
        void when_grade_not_found_expect_empty_optional_returned() {
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.findByIdAndTeacherId(99, 1)).thenReturn(Optional.empty());

            final Optional<ExerciseStudentGrade> result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.findByIdAndTeacherId(99, 1);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_grade_expect_grade_saved_and_cache_evicted() {
            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().studentId(5).exerciseId(100).grade(8.0).build();
            final ExerciseStudentGradeEntity entity = new ExerciseStudentGradeEntity();
            final ExerciseStudentGradeEntity savedEntity = new ExerciseStudentGradeEntity(1, 5, 100, 8.0, null, null);
            final ExerciseStudentGrade saved = ExerciseStudentGrade.builder().id(1).studentId(5).exerciseId(100).grade(8.0).build();
            final ExerciseEntity exerciseEntity = new ExerciseEntity(100, 10, "Exam", "Desc", 1, 30, 10, null);
            final SubjectClassEntity scEntity = new SubjectClassEntity(10, 1, 1, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Math", 1, null);
            final StudentEntity studentEntity = new StudentEntity();

            studentEntity.setId(5);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García");

            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeMapper.toEntity(grade)).thenReturn(entity);
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.save(entity)).thenReturn(savedEntity);
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeMapper.toModel(savedEntity)).thenReturn(saved);
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(100))).thenReturn(List.of(1));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findById(100)).thenReturn(Optional.of(exerciseEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectClassJPARepository.findById(10)).thenReturn(Optional.of(scEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.studentJPARepository.findById(5)).thenReturn(Optional.of(studentEntity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentJPARepository.findByGradeId(1)).thenReturn(List.of());
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentDocumentMapper.toModelList(List.of())).thenReturn(List.of());

            final ExerciseStudentGrade result = ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.save(grade);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(ExerciseStudentGradeRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class SoftDelete {

        @Test
        void when_grade_exists_expect_jpa_delegated_and_cache_evicted() {
            final ExerciseStudentGradeEntity entity = new ExerciseStudentGradeEntity(1, 5, 100, 8.0, null, null);

            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.findById(1)).thenReturn(Optional.of(entity));
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(100))).thenReturn(List.of(1));

            ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.softDelete(1);

            verify(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository).softDeleteById(1);
            verify(ExerciseStudentGradeRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 1);
        }
    }

    @Nested
    class ExistsByStudentIdAndExerciseIdAndDeletionDateIsNull {

        @Test
        void when_grade_exists_expect_true_returned() {
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100)).thenReturn(true);

            assertThat(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100)).isTrue();
        }

        @Test
        void when_grade_does_not_exist_expect_false_returned() {
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100)).thenReturn(false);

            assertThat(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(5, 100)).isFalse();
        }
    }

    @Nested
    class SoftDeleteByExerciseIds {

        @Test
        void when_list_is_not_empty_expect_jpa_delegated_and_cache_evicted() {
            final List<Integer> exerciseIds = List.of(1, 2, 3);

            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(exerciseIds)).thenReturn(List.of(10));

            ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.softDeleteByExerciseIds(exerciseIds);

            verify(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository).softDeleteByExerciseIds(exerciseIds);
            verify(ExerciseStudentGradeRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 10);
        }

        @Test
        void when_list_is_empty_expect_no_jpa_interaction() {
            ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.softDeleteByExerciseIds(List.of());

            verify(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository, never()).softDeleteByExerciseIds(any());
        }

        @Test
        void when_list_is_null_expect_no_jpa_interaction() {
            ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.softDeleteByExerciseIds(null);

            verify(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository, never()).softDeleteByExerciseIds(any());
        }
    }

    @Nested
    class SoftDeleteByStudentIdAndClassId {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted() {
            ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.softDeleteByStudentIdAndClassId(5, 10);

            verify(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository).softDeleteByStudentIdAndClassId(5, 10);
            verify(ExerciseStudentGradeRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 10);
        }
    }

    @Nested
    class SoftDeleteByStudentId {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted_for_all_classes() {
            when(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository.findDistinctClassIdsByStudentId(5)).thenReturn(List.of(10, 20));

            ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeRepository.softDeleteByStudentId(5);

            verify(ExerciseStudentGradeRepositoryImplTest.this.exerciseStudentGradeJPARepository).softDeleteByStudentId(5);
            verify(ExerciseStudentGradeRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 10);
            verify(ExerciseStudentGradeRepositoryImplTest.this.cacheEvictionService).evict("exerciseStudentGradesByClass", 20);
        }
    }
}
