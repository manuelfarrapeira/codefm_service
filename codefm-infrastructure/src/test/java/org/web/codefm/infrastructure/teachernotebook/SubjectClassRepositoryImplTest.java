package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;
import org.web.codefm.infrastructure.mapper.SubjectClassMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectClassRepositoryImplTest {

    private SubjectClassRepositoryImpl subjectClassRepository;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private ClassJPARepository classJPARepository;

    @Mock
    private SubjectClassMapper subjectClassMapper;

    @Mock
    private ClassMapper classMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID_1 = 100;
    private static final Integer SUBJECT_ID_2 = 101;

    @BeforeEach
    void beforeEach() {
        this.subjectClassRepository = new SubjectClassRepositoryImpl(this.subjectClassJPARepository,
                this.subjectJPARepository, this.classJPARepository, this.subjectClassMapper, this.classMapper,
                this.cacheEvictionService);
    }

    @Nested
    class FindSubjectsByClassId {

        @Test
        void when_associations_exist_expect_subjects_returned() {
            final SubjectClassEntity scEntity1 = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectClassEntity scEntity2 = new SubjectClassEntity(2, SUBJECT_ID_2, CLASS_ID, null);
            final List<SubjectClassEntity> scEntities = Arrays.asList(scEntity1, scEntity2);
            final SubjectEntity subjectEntity1 = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
            final SubjectEntity subjectEntity2 = new SubjectEntity(SUBJECT_ID_2, "Science", TEACHER_ID, null);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(scEntities);
            when(SubjectClassRepositoryImplTest.this.subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1,
                    SUBJECT_ID_2))).thenReturn(Arrays.asList(subjectEntity1, subjectEntity2));

            final List<SubjectClassDetail> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findSubjectsByClassId(CLASS_ID);

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getSubjectClassId()).isEqualTo(1);
            assertThat(result.get(0).getSubjectId()).isEqualTo(SUBJECT_ID_1);
            assertThat(result.get(0).getSubjectName()).isEqualTo("Math");
            assertThat(result.get(1).getSubjectClassId()).isEqualTo(2);
            assertThat(result.get(1).getSubjectId()).isEqualTo(SUBJECT_ID_2);
            assertThat(result.get(1).getSubjectName()).isEqualTo("Science");
            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository).findByClassIdAndDeletionDateIsNull(
                    CLASS_ID);
        }

        @Test
        void when_no_associations_exist_expect_empty_list_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(Arrays.asList());

            final List<SubjectClassDetail> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findSubjectsByClassId(CLASS_ID);

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void when_deleted_subjects_exist_expect_deleted_subjects_filtered_out() {
            final SubjectClassEntity scEntity1 = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectClassEntity scEntity2 = new SubjectClassEntity(2, SUBJECT_ID_2, CLASS_ID, null);
            final SubjectEntity activeSubject = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
            final SubjectEntity deletedSubject = new SubjectEntity(SUBJECT_ID_2, "Science", TEACHER_ID,
                    LocalDate.now());
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(Arrays.asList(scEntity1, scEntity2));
            when(SubjectClassRepositoryImplTest.this.subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1,
                    SUBJECT_ID_2))).thenReturn(Arrays.asList(activeSubject, deletedSubject));

            final List<SubjectClassDetail> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findSubjectsByClassId(CLASS_ID);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getSubjectName()).isEqualTo("Math");
            assertThat(result.get(0).getSubjectClassId()).isEqualTo(1);
        }
    }

    @Nested
    class SaveAll {

        @Test
        void when_subject_classes_are_saved_expect_subject_classes_returned() {
            final SubjectClass sc1 = SubjectClass.builder().subjectId(SUBJECT_ID_1).classId(CLASS_ID).build();
            final SubjectClass sc2 = SubjectClass.builder().subjectId(SUBJECT_ID_2).classId(CLASS_ID).build();
            final List<SubjectClass> subjectClasses = Arrays.asList(sc1, sc2);
            final SubjectClassEntity scEntity1 = new SubjectClassEntity(null, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectClassEntity scEntity2 = new SubjectClassEntity(null, SUBJECT_ID_2, CLASS_ID, null);
            final List<SubjectClassEntity> entities = Arrays.asList(scEntity1, scEntity2);
            final SubjectClassEntity savedEntity1 = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectClassEntity savedEntity2 = new SubjectClassEntity(2, SUBJECT_ID_2, CLASS_ID, null);
            final List<SubjectClassEntity> savedEntities = Arrays.asList(savedEntity1, savedEntity2);
            final SubjectClass savedSc1 = SubjectClass.builder().id(1).subjectId(SUBJECT_ID_1).classId(CLASS_ID)
                    .build();
            final SubjectClass savedSc2 = SubjectClass.builder().id(2).subjectId(SUBJECT_ID_2).classId(CLASS_ID)
                    .build();
            final List<SubjectClass> expectedResult = Arrays.asList(savedSc1, savedSc2);
            when(SubjectClassRepositoryImplTest.this.subjectClassMapper.toEntityList(subjectClasses))
                    .thenReturn(entities);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.saveAll(entities))
                    .thenReturn(savedEntities);
            when(SubjectClassRepositoryImplTest.this.subjectClassMapper.toModelList(savedEntities))
                    .thenReturn(expectedResult);

            final List<SubjectClass> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .saveAll(subjectClasses);

            assertThat(result).isNotNull().hasSize(2);
            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository).saveAll(entities);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS,
                    CLASS_ID);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService)
                    .evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }

    @Nested
    class SoftDeleteAll {

        @Test
        void when_subject_classes_are_soft_deleted_expect_repository_called_without_exception() {
            final List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
            doNothing().when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository)
                    .softDeleteByClassIdAndSubjectIds(CLASS_ID, subjectIds);
            final ThrowingCallable callable = () -> SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .softDeleteAll(CLASS_ID, subjectIds);

            assertThatCode(callable).doesNotThrowAnyException();
            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository)
                    .softDeleteByClassIdAndSubjectIds(CLASS_ID, subjectIds);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS,
                    CLASS_ID);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService)
                    .evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }

    @Nested
    class ExistsBySubjectIdAndClassIdAndDeletionDateIsNull {

        @Test
        void when_association_exists_expect_true_returned() {
            final SubjectClassEntity entity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository
                    .findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(Optional.of(entity));

            final boolean result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID);

            assertThat(result).isTrue();
        }

        @Test
        void when_association_does_not_exist_expect_false_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository
                    .findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(Optional.empty());

            final boolean result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class FindAllClassesWithSubjectsByTeacherId {

        @Test
        void when_classes_exist_expect_classes_with_subjects_returned() {
            final List<Integer> classIds = Arrays.asList(CLASS_ID);
            final ClassEntity classEntity = new ClassEntity(CLASS_ID, 1, "1A", "24/25", null);
            final List<ClassEntity> classEntities = Arrays.asList(classEntity);
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").schoolYear("24/25").build();
            final SubjectClassEntity scEntity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectEntity subjectEntity = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findClassIdsByTeacherId(TEACHER_ID))
                    .thenReturn(classIds);
            when(SubjectClassRepositoryImplTest.this.classJPARepository.findAllById(classIds))
                    .thenReturn(classEntities);
            when(SubjectClassRepositoryImplTest.this.classMapper.toModel(classEntity)).thenReturn(clazz);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(Arrays.asList(scEntity));
            when(SubjectClassRepositoryImplTest.this.subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1)))
                    .thenReturn(Arrays.asList(subjectEntity));

            final List<ClassWithSubjects> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findAllClassesWithSubjectsByTeacherId(TEACHER_ID);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getClassData().getId()).isEqualTo(CLASS_ID);
            assertThat(result.get(0).getSubjects()).hasSize(1);
            assertThat(result.get(0).getSubjects().get(0).getSubjectClassId()).isEqualTo(1);
            assertThat(result.get(0).getSubjects().get(0).getSubjectId()).isEqualTo(SUBJECT_ID_1);
            assertThat(result.get(0).getSubjects().get(0).getSubjectName()).isEqualTo("Math");
        }

        @Test
        void when_no_classes_exist_expect_empty_list_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findClassIdsByTeacherId(TEACHER_ID))
                    .thenReturn(Arrays.asList());

            final List<ClassWithSubjects> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findAllClassesWithSubjectsByTeacherId(TEACHER_ID);

            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        void when_deleted_classes_exist_expect_deleted_classes_filtered_out() {
            final List<Integer> classIds = Arrays.asList(CLASS_ID, 20);
            final ClassEntity activeClass = new ClassEntity(CLASS_ID, 1, "1A", "24/25", null);
            final ClassEntity deletedClass = new ClassEntity(20, 1, "2B", "24/25", LocalDate.now());
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").schoolYear("24/25").build();
            final SubjectClassEntity scEntity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectEntity subjectEntity = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findClassIdsByTeacherId(TEACHER_ID))
                    .thenReturn(classIds);
            when(SubjectClassRepositoryImplTest.this.classJPARepository.findAllById(classIds))
                    .thenReturn(Arrays.asList(activeClass, deletedClass));
            when(SubjectClassRepositoryImplTest.this.classMapper.toModel(activeClass)).thenReturn(clazz);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(Arrays.asList(scEntity));
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(20))
                    .thenReturn(Collections.emptyList());
            when(SubjectClassRepositoryImplTest.this.subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1)))
                    .thenReturn(Arrays.asList(subjectEntity));

            final List<ClassWithSubjects> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findAllClassesWithSubjectsByTeacherId(TEACHER_ID);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getClassData().getId()).isEqualTo(CLASS_ID);
        }
    }

    @Nested
    class SoftDeleteByClassId {

        @Test
        void when_subject_classes_are_soft_deleted_expect_repository_called() {
            doNothing().when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository).softDeleteByClassId(
                    CLASS_ID);

            SubjectClassRepositoryImplTest.this.subjectClassRepository.softDeleteByClassId(CLASS_ID);

            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository).softDeleteByClassId(CLASS_ID);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS,
                    CLASS_ID);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService)
                    .evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }

    @Nested
    class SoftDeleteBySubjectId {

        @Test
        void when_subject_classes_are_soft_deleted_expect_repository_called() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository
                    .findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(SUBJECT_ID_1))
                    .thenReturn(Arrays.asList(CLASS_ID, 20));
            doNothing().when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository)
                    .softDeleteBySubjectId(SUBJECT_ID_1);

            SubjectClassRepositoryImplTest.this.subjectClassRepository.softDeleteBySubjectId(SUBJECT_ID_1);

            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository)
                    .findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(SUBJECT_ID_1);
            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository).softDeleteBySubjectId(SUBJECT_ID_1);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS,
                    CLASS_ID);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS,
                    20);
            verify(SubjectClassRepositoryImplTest.this.cacheEvictionService)
                    .evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }

    @Nested
    class FindActiveIdsByClassId {

        @Test
        void when_active_ids_exist_expect_ids_returned() {
            final List<Integer> expectedIds = Arrays.asList(1, 2, 3);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findIdsByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(expectedIds);

            final List<Integer> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findActiveIdsByClassId(CLASS_ID);

            assertThat(result).hasSize(3).isEqualTo(expectedIds);
            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository).findIdsByClassIdAndDeletionDateIsNull(
                    CLASS_ID);
        }

        @Test
        void when_no_active_ids_exist_expect_empty_list_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findIdsByClassIdAndDeletionDateIsNull(
                    CLASS_ID)).thenReturn(Collections.emptyList());

            final List<Integer> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findActiveIdsByClassId(CLASS_ID);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class FindActiveIdsBySubjectId {

        @Test
        void when_active_ids_exist_expect_ids_returned() {
            final List<Integer> expectedIds = Arrays.asList(5, 6);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findIdsBySubjectIdAndDeletionDateIsNull(
                    SUBJECT_ID_1)).thenReturn(expectedIds);

            final List<Integer> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findActiveIdsBySubjectId(SUBJECT_ID_1);

            assertThat(result).hasSize(2).isEqualTo(expectedIds);
            verify(SubjectClassRepositoryImplTest.this.subjectClassJPARepository)
                    .findIdsBySubjectIdAndDeletionDateIsNull(SUBJECT_ID_1);
        }

        @Test
        void when_no_active_ids_exist_expect_empty_list_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findIdsBySubjectIdAndDeletionDateIsNull(
                    SUBJECT_ID_1)).thenReturn(Collections.emptyList());

            final List<Integer> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findActiveIdsBySubjectId(SUBJECT_ID_1);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class FindIdBySubjectIdAndClassId {

        @Test
        void when_association_exists_expect_id_returned() {
            final SubjectClassEntity entity = new SubjectClassEntity(42, SUBJECT_ID_1, CLASS_ID, null);
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository
                    .findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(Optional.of(entity));

            final Optional<Integer> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findIdBySubjectIdAndClassId(SUBJECT_ID_1, CLASS_ID);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        void when_association_does_not_exist_expect_empty_optional_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository
                    .findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(Optional.empty());

            final Optional<Integer> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findIdBySubjectIdAndClassId(SUBJECT_ID_1, CLASS_ID);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class FindById {

        @Test
        void when_subject_class_exists_expect_subject_class_returned() {
            final SubjectClassEntity entity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
            final SubjectClass expectedModel = SubjectClass.builder().id(1).subjectId(SUBJECT_ID_1).classId(CLASS_ID)
                    .build();
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findById(1))
                    .thenReturn(Optional.of(entity));
            when(SubjectClassRepositoryImplTest.this.subjectClassMapper.toModel(entity)).thenReturn(expectedModel);

            final Optional<SubjectClass> result = SubjectClassRepositoryImplTest.this.subjectClassRepository.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1);
            assertThat(result.get().getSubjectId()).isEqualTo(SUBJECT_ID_1);
            assertThat(result.get().getClassId()).isEqualTo(CLASS_ID);
        }

        @Test
        void when_subject_class_does_not_exist_expect_empty_optional_returned() {
            when(SubjectClassRepositoryImplTest.this.subjectClassJPARepository.findById(999)).thenReturn(Optional.empty());

            final Optional<SubjectClass> result = SubjectClassRepositoryImplTest.this.subjectClassRepository
                    .findById(999);

            assertThat(result).isNotPresent();
        }
    }
}
