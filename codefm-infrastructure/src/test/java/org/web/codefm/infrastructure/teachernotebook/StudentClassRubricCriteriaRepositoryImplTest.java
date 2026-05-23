package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.*;
import org.web.codefm.infrastructure.jpa.teachernotebook.*;
import org.web.codefm.infrastructure.mapper.StudentClassRubricCriteriaMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassRubricCriteriaRepositoryImplTest {

    private StudentClassRubricCriteriaRepositoryImpl studentClassRubricCriteriaRepository;

    @Mock
    private StudentClassRubricCriteriaJPARepository studentClassRubricCriteriaJPARepository;

    @Mock
    private ClassRubricJPARepository classRubricJPARepository;

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private SkillRubricCriteriaJPARepository skillRubricCriteriaJPARepository;

    @Mock
    private SkillRubricJPARepository skillRubricJPARepository;

    @Mock
    private StudentClassRubricCriteriaMapper studentClassRubricCriteriaMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.studentClassRubricCriteriaRepository = new StudentClassRubricCriteriaRepositoryImpl(
                this.studentClassRubricCriteriaJPARepository,
                this.classRubricJPARepository,
                this.studentJPARepository,
                this.skillRubricCriteriaJPARepository,
                this.skillRubricJPARepository,
                this.studentClassRubricCriteriaMapper,
                this.cacheEvictionService);
    }

    @Nested
    class FindByClassId {

        @Test
        void when_criteria_exist_expect_enriched_criteria_returned() {
            final Integer classId = 10;
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndDeletionDateIsNull(classId)).thenReturn(List.of(entity));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper
                    .toModelList(List.of(entity))).thenReturn(List.of(model));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository
                    .findByIdAndDeletionDateIsNull(100)).thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricJPARepository.findById(50))
                    .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric Title", 1, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(200))
                    .thenReturn(Optional.of(new StudentEntity(200, 1, "Juan", "García", null, "M", null, null, null, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Lo hace bien", "Notable", 50, 5, 10, null)));

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassId(classId);
            final StudentClassRubricCriteria criteria = result.get(0);

            assertThat(result).hasSize(1);
            assertThat(criteria.getRubricId()).isEqualTo(50);
            assertThat(criteria.getRubricTitle()).isEqualTo("Rubric Title");
            assertThat(criteria.getStudentName()).isEqualTo("Juan");
            assertThat(criteria.getStudentSurnames()).isEqualTo("García");
            assertThat(criteria.getCriterionDescription()).isEqualTo("Lo hace bien");
            assertThat(criteria.getQualification()).isEqualTo("Notable");
            assertThat(criteria.getGradeStart()).isEqualTo(5);
            assertThat(criteria.getGradeEnd()).isEqualTo(10);
        }

        @Test
        void when_no_criteria_exist_expect_empty_list_returned() {
            final Integer classId = 10;

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndDeletionDateIsNull(classId)).thenReturn(List.of());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toModelList(List.of()))
                    .thenReturn(List.of());

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassId(classId);

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void when_class_rubric_not_found_expect_partial_enrichment() {
            final Integer classId = 10;
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndDeletionDateIsNull(classId)).thenReturn(List.of(entity));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper
                    .toModelList(List.of(entity))).thenReturn(List.of(model));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                    .thenReturn(Optional.empty());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(200))
                    .thenReturn(Optional.empty());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.empty());

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassId(classId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRubricId()).isNull();
            assertThat(result.get(0).getRubricTitle()).isNull();
            assertThat(result.get(0).getStudentName()).isNull();
            assertThat(result.get(0).getCriterionDescription()).isNull();
        }

        @Test
        void when_rubric_entity_not_found_expect_partial_enrichment() {
            final Integer classId = 10;
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndDeletionDateIsNull(classId)).thenReturn(List.of(entity));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper
                    .toModelList(List.of(entity))).thenReturn(List.of(model));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository
                    .findByIdAndDeletionDateIsNull(100)).thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricJPARepository.findById(50))
                    .thenReturn(Optional.empty());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(200))
                    .thenReturn(Optional.empty());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.empty());

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassId(classId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRubricId()).isEqualTo(50);
            assertThat(result.get(0).getRubricTitle()).isNull();
        }

        @Test
        void when_multiple_criteria_exist_expect_all_criteria_enriched() {
            final Integer classId = 10;
            final StudentClassRubricCriteriaEntity entity1 = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
            final StudentClassRubricCriteriaEntity entity2 = new StudentClassRubricCriteriaEntity(2, 101, 201, 301, null);
            final StudentClassRubricCriteria model1 = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).build();
            final StudentClassRubricCriteria model2 = StudentClassRubricCriteria.builder()
                    .id(2).classRubricId(101).studentId(201).criterionId(301).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndDeletionDateIsNull(classId)).thenReturn(Arrays.asList(entity1, entity2));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper
                    .toModelList(Arrays.asList(entity1, entity2))).thenReturn(Arrays.asList(model1, model2));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository
                    .findByIdAndDeletionDateIsNull(100)).thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository
                    .findByIdAndDeletionDateIsNull(101)).thenReturn(Optional.of(new ClassRubricEntity(101, classId, 51, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricJPARepository.findById(50))
                    .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric A", 1, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricJPARepository.findById(51))
                    .thenReturn(Optional.of(new SkillRubricEntity(51, "Rubric B", 2, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(200))
                    .thenReturn(Optional.of(new StudentEntity(200, 1, "Juan", "García", null, "M", null, null, null, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(201))
                    .thenReturn(Optional.of(new StudentEntity(201, 1, "Ana", "López", null, "F", null, null, null, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Mal", "Insuficiente", 50, 0, 4, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(301))
                    .thenReturn(Optional.of(new SkillRubricCriteriaEntity(301, "Bien", "Notable", 51, 7, 10, null)));

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassId(classId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRubricTitle()).isEqualTo("Rubric A");
            assertThat(result.get(0).getStudentName()).isEqualTo("Juan");
            assertThat(result.get(0).getCriterionDescription()).isEqualTo("Mal");
            assertThat(result.get(1).getRubricTitle()).isEqualTo("Rubric B");
            assertThat(result.get(1).getStudentName()).isEqualTo("Ana");
            assertThat(result.get(1).getCriterionDescription()).isEqualTo("Bien");
        }
    }

    @Nested
    class FindByClassIdAndStudentId {

        @Test
        void when_criteria_exist_expect_enriched_criteria_returned() {
            final Integer classId = 10;
            final Integer studentId = 200;
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(1, 100, studentId, 300,
                    null);
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(studentId).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndStudentIdAndDeletionDateIsNull(classId, studentId)).thenReturn(List.of(entity));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper
                    .toModelList(List.of(entity))).thenReturn(List.of(model));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository
                    .findByIdAndDeletionDateIsNull(100)).thenReturn(Optional.of(new ClassRubricEntity(100, classId, 50, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricJPARepository.findById(50))
                    .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric Title", 1, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(studentId))
                    .thenReturn(Optional.of(new StudentEntity(studentId, 1, "Ana", "López", null, "F", null, null, null,
                            null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Lo hace regular", "Suficiente", 50, 5, 6,
                            null)));

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassIdAndStudentId(classId, studentId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentName()).isEqualTo("Ana");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("López");
            assertThat(result.get(0).getCriterionDescription()).isEqualTo("Lo hace regular");
            assertThat(result.get(0).getQualification()).isEqualTo("Suficiente");
        }

        @Test
        void when_no_criteria_exist_expect_empty_list_returned() {
            final Integer classId = 10;
            final Integer studentId = 200;

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByClassIdAndStudentIdAndDeletionDateIsNull(classId, studentId)).thenReturn(List.of());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toModelList(List.of()))
                    .thenReturn(List.of());

            final List<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByClassIdAndStudentId(classId, studentId);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_criteria_found_expect_criteria_returned() {
            final Integer id = 1;
            final Integer teacherId = 5;
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(id, 100, 200, 300, null);
            final StudentClassRubricCriteria model = StudentClassRubricCriteria.builder()
                    .id(id).classRubricId(100).studentId(200).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)).thenReturn(Optional.of(entity));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toModel(entity))
                    .thenReturn(model);

            final Optional<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByIdAndTeacherId(id, teacherId);

            assertThat(result).isPresent();
            assertThat(result.orElseThrow().getId()).isEqualTo(id);
        }

        @Test
        void when_criteria_not_found_expect_empty_optional() {
            final Integer id = 999;
            final Integer teacherId = 5;

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)).thenReturn(Optional.empty());

            final Optional<StudentClassRubricCriteria> result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.findByIdAndTeacherId(id, teacherId);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_criteria_saved_and_cache_evicted() {
            final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                    .classRubricId(100).studentId(200).criterionId(300).build();
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(null, 100, 200, 300,
                    null);
            final StudentClassRubricCriteriaEntity saved = new StudentClassRubricCriteriaEntity(1, 100, 200, 300, null);
            final StudentClassRubricCriteria savedModel = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(100).studentId(200).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toEntity(criteria))
                    .thenReturn(entity);
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository.save(entity))
                    .thenReturn(saved);
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toModel(saved))
                    .thenReturn(savedModel);
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findByIdAndDeletionDateIsNull(100))
                    .thenReturn(Optional.of(new ClassRubricEntity(100, 10, 50, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByIds(
                    List.of(100))).thenReturn(List.of(10));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricJPARepository.findById(50))
                    .thenReturn(Optional.of(new SkillRubricEntity(50, "Rubric Title", 1, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(200))
                    .thenReturn(Optional.of(new StudentEntity(200, 1, "Juan", "García", null, "M", null, null, null, null)));
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.of(new SkillRubricCriteriaEntity(300, "Lo hace bien", "Notable", 50, 7, 10,
                            null)));

            final StudentClassRubricCriteria result = StudentClassRubricCriteriaRepositoryImplTest.this
                    .studentClassRubricCriteriaRepository.save(criteria);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getRubricTitle()).isEqualTo("Rubric Title");
            assertThat(result.getStudentName()).isEqualTo("Juan");
            assertThat(result.getStudentSurnames()).isEqualTo("García");
            assertThat(result.getCriterionDescription()).isEqualTo("Lo hace bien");
            assertThat(result.getQualification()).isEqualTo("Notable");
            assertThat(result.getGradeStart()).isEqualTo(7);
            assertThat(result.getGradeEnd()).isEqualTo(10);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository).save(entity);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 10);
        }

        @Test
        void when_class_rubric_not_found_expect_criteria_saved_without_cache_eviction() {
            final StudentClassRubricCriteria criteria = StudentClassRubricCriteria.builder()
                    .classRubricId(999).studentId(200).criterionId(300).build();
            final StudentClassRubricCriteriaEntity entity = new StudentClassRubricCriteriaEntity(null, 999, 200, 300,
                    null);
            final StudentClassRubricCriteriaEntity saved = new StudentClassRubricCriteriaEntity(1, 999, 200, 300, null);
            final StudentClassRubricCriteria savedModel = StudentClassRubricCriteria.builder()
                    .id(1).classRubricId(999).studentId(200).criterionId(300).build();

            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toEntity(criteria))
                    .thenReturn(entity);
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository.save(entity))
                    .thenReturn(saved);
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaMapper.toModel(saved))
                    .thenReturn(savedModel);
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findByIdAndDeletionDateIsNull(999))
                    .thenReturn(Optional.empty());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByIds(
                    List.of(999))).thenReturn(List.of());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentJPARepository.findById(200))
                    .thenReturn(Optional.empty());
            when(StudentClassRubricCriteriaRepositoryImplTest.this.skillRubricCriteriaJPARepository.findById(300))
                    .thenReturn(Optional.empty());

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.save(criteria);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService, never()).evict(anyString(), any());
        }
    }

    @Nested
    class SoftDeleteById {

        @Test
        void when_class_id_found_expect_delegation_and_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository.findClassIdById(1))
                    .thenReturn(Optional.of(10));

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteById(1);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteById(1);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 10);
        }

        @Test
        void when_class_id_not_found_expect_delegation_without_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository.findClassIdById(999))
                    .thenReturn(Optional.empty());

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteById(999);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteById(999);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService, never()).evict(anyString(), any());
        }
    }

    @Nested
    class SoftDeleteByClassRubricId {

        @Test
        void when_class_ids_found_expect_delegation_and_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByIds(
                    List.of(100))).thenReturn(List.of(10));

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteByClassRubricId(
                    100);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteByClassRubricId(100);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 10);
        }

        @Test
        void when_class_ids_not_found_expect_delegation_without_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByIds(
                    List.of(999))).thenReturn(List.of());

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteByClassRubricId(
                    999);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteByClassRubricId(999);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService, never()).evict(anyString(), any());
        }
    }

    @Nested
    class SoftDeleteByCriterionId {

        @Test
        void when_classes_affected_expect_delegation_and_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findDistinctClassIdsByCriterionId(300)).thenReturn(Arrays.asList(10, 20));

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteByCriterionId(
                    300);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteByCriterionId(300);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 10);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 20);
        }

        @Test
        void when_no_classes_affected_expect_delegation_without_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .findDistinctClassIdsByCriterionId(999)).thenReturn(List.of());

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteByCriterionId(
                    999);

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteByCriterionId(999);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService, never()).evict(anyString(), any());
        }
    }

    @Nested
    class SoftDeleteByClassRubricIds {

        @Test
        void when_ids_not_empty_expect_delegation_and_cache_eviction() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.classRubricJPARepository.findDistinctClassIdsByIds(
                    Arrays.asList(1, 2))).thenReturn(Arrays.asList(10, 20));

            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteByClassRubricIds(
                    Arrays.asList(1, 2));

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository)
                    .softDeleteByClassRubricIds(Arrays.asList(1, 2));
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 10);
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService)
                    .evict("studentClassRubricCriteriaByClass", 20);
        }

        @Test
        void when_ids_empty_expect_no_delegation() {
            StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository.softDeleteByClassRubricIds(
                    List.of());

            verify(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository, never())
                    .softDeleteByClassRubricIds(any());
            verify(StudentClassRubricCriteriaRepositoryImplTest.this.cacheEvictionService, never()).evict(anyString(), any());
        }
    }

    @Nested
    class ExistsByClassRubricIdAndStudentIdAndDeletionDateIsNull {

        @Test
        void when_criteria_exists_expect_true() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200)).thenReturn(true);

            final boolean result = StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository
                    .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200);

            assertThat(result).isTrue();
        }

        @Test
        void when_criteria_not_exists_expect_false() {
            when(StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaJPARepository
                    .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200)).thenReturn(false);

            final boolean result = StudentClassRubricCriteriaRepositoryImplTest.this.studentClassRubricCriteriaRepository
                    .existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(100, 200);

            assertThat(result).isFalse();
        }
    }
}

