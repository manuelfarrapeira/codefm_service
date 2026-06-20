package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseDocumentEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseDocumentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseDocumentMapper;
import org.web.codefm.infrastructure.mapper.ExerciseMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseRepositoryImplTest {

    private ExerciseRepositoryImpl exerciseRepository;

    @Mock
    private ExerciseJPARepository exerciseJPARepository;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private ExerciseDocumentJPARepository exerciseDocumentJPARepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @Mock
    private ExerciseDocumentMapper exerciseDocumentMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.exerciseRepository = new ExerciseRepositoryImpl(
                this.exerciseJPARepository,
                this.subjectClassJPARepository,
                this.subjectJPARepository,
                this.exerciseDocumentJPARepository,
                this.exerciseMapper,
                this.exerciseDocumentMapper,
                this.cacheEvictionService
        );
    }

    @Nested
    class FindByClassId {

        @Test
        void when_subject_classes_exist_expect_exercises_with_subject_data_returned() {
            final Integer classId = 1;
            final SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, classId, null);
            final ExerciseEntity exerciseEntity = new ExerciseEntity(1, 5, "Exam", "Desc", 1, 30, 10, null);
            final Exercise exercise = Exercise.builder().id(1).subjectClassId(5).title("Exam").percentageGrade(30).maxGrade(10).build();
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);
            final List<ExerciseDocumentEntity> documentEntities = List.of();
            final List<ExerciseDocument> documents = List.of();

            when(ExerciseRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId)).thenReturn(List.of(scEntity));
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.findBySubjectClassIdInAndDeletionDateIsNull(List.of(5))).thenReturn(List.of(exerciseEntity));
            when(ExerciseRepositoryImplTest.this.exerciseMapper.toModelList(List.of(exerciseEntity))).thenReturn(List.of(exercise));
            when(ExerciseRepositoryImplTest.this.subjectJPARepository.findAllById(List.of(1))).thenReturn(List.of(subjectEntity));
            when(ExerciseRepositoryImplTest.this.exerciseDocumentJPARepository.findByExerciseIdIn(List.of(1))).thenReturn(documentEntities);
            when(ExerciseRepositoryImplTest.this.exerciseDocumentMapper.toModelList(documentEntities)).thenReturn(documents);

            final List<Exercise> result = ExerciseRepositoryImplTest.this.exerciseRepository.findByClassId(classId);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getSubjectId()).isEqualTo(1);
            assertThat(result.get(0).getSubjectName()).isEqualTo("Mathematics");
            verify(ExerciseRepositoryImplTest.this.subjectClassJPARepository).findByClassIdAndDeletionDateIsNull(classId);
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).findBySubjectClassIdInAndDeletionDateIsNull(List.of(5));
        }

        @Test
        void when_no_subject_classes_expect_empty_list_returned() {
            final Integer classId = 1;

            when(ExerciseRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId)).thenReturn(new ArrayList<>());

            final List<Exercise> result = ExerciseRepositoryImplTest.this.exerciseRepository.findByClassId(classId);

            assertThat(result).isNotNull().isEmpty();
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository, never()).findBySubjectClassIdInAndDeletionDateIsNull(any());
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_exercise_found_expect_exercise_with_subject_data_returned() {
            final Integer id = 1;
            final Integer teacherId = 1;
            final ExerciseEntity entity = new ExerciseEntity(id, 5, "Exam", "Desc", 1, 30, 10, null);
            final Exercise exercise = Exercise.builder().id(id).subjectClassId(5).title("Exam").percentageGrade(30).maxGrade(10).build();
            final SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, 10, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
            when(ExerciseRepositoryImplTest.this.exerciseMapper.toModel(entity)).thenReturn(exercise);
            when(ExerciseRepositoryImplTest.this.subjectClassJPARepository.findById(5)).thenReturn(Optional.of(scEntity));
            when(ExerciseRepositoryImplTest.this.subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));

            final Optional<Exercise> result = ExerciseRepositoryImplTest.this.exerciseRepository.findByIdAndTeacherId(id, teacherId);

            assertThat(result).isPresent();
            assertThat(result.orElseThrow().getId()).isEqualTo(id);
            assertThat(result.orElseThrow().getSubjectId()).isEqualTo(1);
            assertThat(result.orElseThrow().getSubjectName()).isEqualTo("Mathematics");
        }

        @Test
        void when_exercise_not_found_expect_empty_optional_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.findByIdAndTeacherId(99, 1)).thenReturn(Optional.empty());

            final Optional<Exercise> result = ExerciseRepositoryImplTest.this.exerciseRepository.findByIdAndTeacherId(99, 1);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_exercise_expect_exercise_saved_and_cache_evicted() {
            final Exercise exercise = Exercise.builder().subjectClassId(5).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            final ExerciseEntity entity = new ExerciseEntity();
            final ExerciseEntity savedEntity = new ExerciseEntity(1, 5, "Exam", null, 1, 30, 10, null);
            final Exercise savedExercise = Exercise.builder().id(1).subjectClassId(5).title("Exam").quarter(1).percentageGrade(30).maxGrade(10).build();
            final SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, 10, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

            when(ExerciseRepositoryImplTest.this.exerciseMapper.toEntity(exercise)).thenReturn(entity);
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.save(entity)).thenReturn(savedEntity);
            when(ExerciseRepositoryImplTest.this.exerciseMapper.toModel(savedEntity)).thenReturn(savedExercise);
            when(ExerciseRepositoryImplTest.this.subjectClassJPARepository.findById(5)).thenReturn(Optional.of(scEntity));
            when(ExerciseRepositoryImplTest.this.subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));

            final Exercise result = ExerciseRepositoryImplTest.this.exerciseRepository.save(exercise);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getSubjectId()).isEqualTo(1);
            assertThat(result.getSubjectName()).isEqualTo("Mathematics");
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).save(entity);
            verify(ExerciseRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 10);
        }
    }

    @Nested
    class Update {

        @Test
        void when_valid_exercise_expect_exercise_updated_and_cache_evicted() {
            final Exercise exercise = Exercise.builder().id(1).subjectClassId(5).title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
            final ExerciseEntity entity = new ExerciseEntity(1, 5, "Updated", null, 2, 50, 12, null);
            final ExerciseEntity savedEntity = new ExerciseEntity(1, 5, "Updated", null, 2, 50, 12, null);
            final Exercise updatedExercise = Exercise.builder().id(1).subjectClassId(5).title("Updated").quarter(2).percentageGrade(50).maxGrade(12).build();
            final SubjectClassEntity scEntity = new SubjectClassEntity(5, 1, 10, null);
            final SubjectEntity subjectEntity = new SubjectEntity(1, "Mathematics", 1, null);

            when(ExerciseRepositoryImplTest.this.exerciseMapper.toEntity(exercise)).thenReturn(entity);
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.save(entity)).thenReturn(savedEntity);
            when(ExerciseRepositoryImplTest.this.exerciseMapper.toModel(savedEntity)).thenReturn(updatedExercise);
            when(ExerciseRepositoryImplTest.this.subjectClassJPARepository.findById(5)).thenReturn(Optional.of(scEntity));
            when(ExerciseRepositoryImplTest.this.subjectJPARepository.findById(1)).thenReturn(Optional.of(subjectEntity));

            final Exercise result = ExerciseRepositoryImplTest.this.exerciseRepository.update(exercise);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated");
            assertThat(result.getSubjectId()).isEqualTo(1);
            assertThat(result.getSubjectName()).isEqualTo("Mathematics");
            verify(ExerciseRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 10);
        }
    }

    @Nested
    class SoftDelete {

        @Test
        void when_called_expect_jpa_delegated_and_cache_evicted() {
            final Integer id = 1;

            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(List.of(id))).thenReturn(List.of(10));

            ExerciseRepositoryImplTest.this.exerciseRepository.softDelete(id);

            verify(ExerciseRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 10);
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).softDeleteById(id);
        }
    }

    @Nested
    class SubjectClassBelongsToTeacher {

        @Test
        void when_subject_class_belongs_to_teacher_expect_true_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.subjectClassBelongsToTeacher(5, 1)).thenReturn(true);

            assertThat(ExerciseRepositoryImplTest.this.exerciseRepository.subjectClassBelongsToTeacher(5, 1)).isTrue();
        }

        @Test
        void when_subject_class_does_not_belong_to_teacher_expect_false_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.subjectClassBelongsToTeacher(5, 1)).thenReturn(false);

            assertThat(ExerciseRepositoryImplTest.this.exerciseRepository.subjectClassBelongsToTeacher(5, 1)).isFalse();
        }
    }

    @Nested
    class SoftDeleteBySubjectClassIds {

        @Test
        void when_list_is_not_empty_expect_jpa_delegated_and_cache_evicted() {
            final List<Integer> subjectClassIds = List.of(1, 2, 3);

            when(ExerciseRepositoryImplTest.this.subjectClassJPARepository.findDistinctClassIdsBySubjectClassIds(subjectClassIds)).thenReturn(List.of(10));

            ExerciseRepositoryImplTest.this.exerciseRepository.softDeleteBySubjectClassIds(subjectClassIds);

            verify(ExerciseRepositoryImplTest.this.cacheEvictionService).evict(CacheName.EXERCISES_BY_CLASS, 10);
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).softDeleteBySubjectClassIds(subjectClassIds);
        }

        @Test
        void when_list_is_empty_expect_no_jpa_interaction() {
            ExerciseRepositoryImplTest.this.exerciseRepository.softDeleteBySubjectClassIds(List.of());

            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository, never()).softDeleteBySubjectClassIds(any());
        }

        @Test
        void when_list_is_null_expect_no_jpa_interaction() {
            ExerciseRepositoryImplTest.this.exerciseRepository.softDeleteBySubjectClassIds(null);

            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository, never()).softDeleteBySubjectClassIds(any());
        }
    }

    @Nested
    class FindActiveIdsBySubjectClassIds {

        @Test
        void when_exercises_exist_expect_ids_returned() {
            final List<Integer> subjectClassIds = List.of(1, 2);
            final List<Integer> expectedIds = List.of(10, 20, 30);

            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.findActiveIdsBySubjectClassIds(subjectClassIds)).thenReturn(expectedIds);

            final List<Integer> result = ExerciseRepositoryImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(subjectClassIds);

            assertThat(result).isEqualTo(expectedIds);
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).findActiveIdsBySubjectClassIds(subjectClassIds);
        }

        @Test
        void when_no_exercises_exist_expect_empty_list_returned() {
            final List<Integer> subjectClassIds = List.of(1, 2);

            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.findActiveIdsBySubjectClassIds(subjectClassIds)).thenReturn(List.of());

            final List<Integer> result = ExerciseRepositoryImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(subjectClassIds);

            assertThat(result).isEmpty();
        }

        @Test
        void when_input_list_is_empty_expect_empty_list_returned() {
            final List<Integer> result = ExerciseRepositoryImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(List.of());

            assertThat(result).isEmpty();
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository, never()).findActiveIdsBySubjectClassIds(any());
        }

        @Test
        void when_input_list_is_null_expect_empty_list_returned() {
            final List<Integer> result = ExerciseRepositoryImplTest.this.exerciseRepository.findActiveIdsBySubjectClassIds(null);

            assertThat(result).isEmpty();
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository, never()).findActiveIdsBySubjectClassIds(any());
        }
    }

    @Nested
    class SumPercentageGradeBySubjectClassIdAndQuarter {

        @Test
        void when_exercises_exist_expect_sum_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.sumPercentageGradeBySubjectClassIdAndQuarter(5, 1)).thenReturn(70);

            final Integer result = ExerciseRepositoryImplTest.this.exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(5, 1);

            assertThat(result).isEqualTo(70);
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).sumPercentageGradeBySubjectClassIdAndQuarter(5, 1);
        }

        @Test
        void when_no_exercises_exist_expect_zero_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.sumPercentageGradeBySubjectClassIdAndQuarter(5, 1)).thenReturn(0);

            final Integer result = ExerciseRepositoryImplTest.this.exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarter(5, 1);

            assertThat(result).isZero();
        }
    }

    @Nested
    class SumPercentageGradeBySubjectClassIdAndQuarterExcludingId {

        @Test
        void when_other_exercises_exist_expect_sum_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(5, 1, 100)).thenReturn(50);

            final Integer result = ExerciseRepositoryImplTest.this.exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(5, 1, 100);

            assertThat(result).isEqualTo(50);
            verify(ExerciseRepositoryImplTest.this.exerciseJPARepository).sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(5, 1, 100);
        }

        @Test
        void when_no_other_exercises_exist_expect_zero_returned() {
            when(ExerciseRepositoryImplTest.this.exerciseJPARepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(5, 1, 100)).thenReturn(0);

            final Integer result = ExerciseRepositoryImplTest.this.exerciseRepository.sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(5, 1, 100);

            assertThat(result).isZero();
        }
    }
}
