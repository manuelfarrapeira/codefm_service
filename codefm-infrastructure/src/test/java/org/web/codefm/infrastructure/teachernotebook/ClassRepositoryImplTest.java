package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassRepositoryImplTest {

    private ClassRepositoryImpl classRepository;

    @Mock
    private ClassJPARepository classJPARepository;

    @Mock
    private ClassMapper classMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.classRepository = new ClassRepositoryImpl(this.classJPARepository, this.classMapper, this.cacheEvictionService);
    }

    @Nested
    class FindActiveClassesBySchoolIdAndTeacherId {

        @Test
        void when_active_classes_exist_expect_classes_returned() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final ClassEntity entity1 = new ClassEntity(1, schoolId, "Math Class", "24/25", null);
            final ClassEntity entity2 = new ClassEntity(2, schoolId, "Science Class", "24/25", null);
            final List<ClassEntity> entities = Arrays.asList(entity1, entity2);
            final Class class1 = Class.builder().id(1).schoolId(schoolId).name("Math Class").schoolYear("24/25").build();
            final Class class2 = Class.builder().id(2).schoolId(schoolId).name("Science Class").schoolYear("24/25").build();
            final List<Class> expectedClasses = Arrays.asList(class1, class2);

            when(ClassRepositoryImplTest.this.classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(entities);
            when(ClassRepositoryImplTest.this.classMapper.toModelList(entities)).thenReturn(expectedClasses);

            final List<Class> result = ClassRepositoryImplTest.this.classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

            assertThat(result).isNotNull().hasSize(2);
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_no_classes_found_expect_empty_list() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final List<ClassEntity> entities = List.of();

            when(ClassRepositoryImplTest.this.classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(entities);
            when(ClassRepositoryImplTest.this.classMapper.toModelList(entities)).thenReturn(List.of());

            final List<Class> result = ClassRepositoryImplTest.this.classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toModelList(entities);
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_class_expect_class_saved() {
            final Integer schoolId = 1;
            final Class classToSave = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();
            final ClassEntity entityToSave = new ClassEntity(null, schoolId, "Math Class", "24/25", null);
            final ClassEntity savedEntity = new ClassEntity(1, schoolId, "Math Class", "24/25", null);
            final Class expectedClass = Class.builder()
                    .id(1)
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            when(ClassRepositoryImplTest.this.classMapper.toEntity(classToSave)).thenReturn(entityToSave);
            when(ClassRepositoryImplTest.this.classJPARepository.save(entityToSave)).thenReturn(savedEntity);
            when(ClassRepositoryImplTest.this.classMapper.toModel(savedEntity)).thenReturn(expectedClass);

            final Class result = ClassRepositoryImplTest.this.classRepository.save(classToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Math Class");
            assertThat(result.getSchoolYear()).isEqualTo("24/25");
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toEntity(classToSave);
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).save(entityToSave);
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toModel(savedEntity);
            verify(ClassRepositoryImplTest.this.cacheEvictionService).evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_class_exists_expect_class_returned() {
            final Integer classId = 1;
            final ClassEntity entity = new ClassEntity(classId, 1, "Math Class", "24/25", null);
            final Class expectedClass = Class.builder()
                    .id(classId)
                    .schoolId(1)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndDeletionDateIsNull(classId)).thenReturn(Optional.of(entity));
            when(ClassRepositoryImplTest.this.classMapper.toModel(entity)).thenReturn(expectedClass);

            final Optional<Class> result = ClassRepositoryImplTest.this.classRepository.findById(classId);

            assertThat(result).isNotNull().isPresent();
            assertThat(result.get().getId()).isEqualTo(classId);
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndDeletionDateIsNull(classId);
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toModel(entity);
        }

        @Test
        void when_class_does_not_exist_expect_empty_optional() {
            final Integer classId = 999;

            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndDeletionDateIsNull(classId)).thenReturn(Optional.empty());

            final Optional<Class> result = ClassRepositoryImplTest.this.classRepository.findById(classId);

            assertThat(result).isNotNull().isNotPresent();
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndDeletionDateIsNull(classId);
            verify(ClassRepositoryImplTest.this.classMapper, never()).toModel(any());
        }

        @Test
        void when_class_is_deleted_expect_empty_optional() {
            final Integer classId = 1;

            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndDeletionDateIsNull(classId)).thenReturn(Optional.empty());

            final Optional<Class> result = ClassRepositoryImplTest.this.classRepository.findById(classId);

            assertThat(result).isNotNull().isNotPresent();
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndDeletionDateIsNull(classId);
            verify(ClassRepositoryImplTest.this.classMapper, never()).toModel(any());
        }
    }

    @Nested
    class FindByIdAndTeacherIdAndDeletionDateIsNull {

        @Test
        void when_class_exists_expect_class_returned() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final ClassEntity entity = new ClassEntity(classId, 1, "Math Class", "24/25", null);
            final Class expectedClass = Class.builder()
                    .id(classId)
                    .schoolId(1)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.of(entity));
            when(ClassRepositoryImplTest.this.classMapper.toModel(entity)).thenReturn(expectedClass);

            final Optional<Class> result = ClassRepositoryImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);

            assertThat(result).isNotNull().isPresent();
            assertThat(result.get().getId()).isEqualTo(classId);
            assertThat(result.get().getSchoolId()).isEqualTo(teacherId);
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toModel(entity);
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource({
                "Class does not exist, 999, 1",
                "Class not owned by teacher, 1, 999",
                "Class is deleted, 1, 1"
        })
        void when_class_not_found_or_not_owned_or_deleted_expect_empty_optional(String testCase, Integer classId, Integer teacherId) {
            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.empty());

            final Optional<Class> result = ClassRepositoryImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);

            assertThat(result).isNotNull().isNotPresent();
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
            verify(ClassRepositoryImplTest.this.classMapper, never()).toModel(any());
        }
    }

    @Nested
    class SoftDeleteClass {

        @Test
        void when_class_exists_expect_deletion_date_set_and_class_returned() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final ClassEntity classEntity = new ClassEntity();
            classEntity.setId(classId);
            classEntity.setSchoolId(1);
            classEntity.setName("Test Class");
            classEntity.setSchoolYear("24/25");
            classEntity.setDeletionDate(null);
            final ClassEntity updatedEntity = new ClassEntity();
            updatedEntity.setId(classId);
            updatedEntity.setSchoolId(1);
            updatedEntity.setName("Test Class");
            updatedEntity.setSchoolYear("24/25");
            updatedEntity.setDeletionDate(LocalDate.now());
            final Class expectedClass = Class.builder()
                    .id(classId)
                    .schoolId(1)
                    .name("Test Class")
                    .schoolYear("24/25")
                    .build();

            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.of(classEntity));
            when(ClassRepositoryImplTest.this.classJPARepository.save(any(ClassEntity.class))).thenReturn(updatedEntity);
            when(ClassRepositoryImplTest.this.classMapper.toModel(updatedEntity)).thenReturn(expectedClass);

            final Class result = ClassRepositoryImplTest.this.classRepository.softDeleteClass(classId, teacherId);

            assertThat(result).isNotNull();
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).save(any(ClassEntity.class));
            verify(ClassRepositoryImplTest.this.classMapper, times(1)).toModel(updatedEntity);
            verify(ClassRepositoryImplTest.this.cacheEvictionService).evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }

        @Test
        void when_class_not_found_or_not_owned_expect_exception() {
            final Integer classId = 999;
            final Integer teacherId = 1;

            when(ClassRepositoryImplTest.this.classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.empty());

            final ThrowingCallable callable = () -> ClassRepositoryImplTest.this.classRepository.softDeleteClass(classId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(ClassRepositoryImplTest.this.classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
            verify(ClassRepositoryImplTest.this.classJPARepository, never()).save(any());
        }
    }

    @Nested
    class SoftDeleteBySchoolId {

        @Test
        void when_school_exists_expect_repository_called_and_cache_evicted() {
            final Integer schoolId = 1;

            doNothing().when(ClassRepositoryImplTest.this.classJPARepository).softDeleteBySchoolId(schoolId);

            ClassRepositoryImplTest.this.classRepository.softDeleteBySchoolId(schoolId);

            verify(ClassRepositoryImplTest.this.classJPARepository).softDeleteBySchoolId(schoolId);
            verify(ClassRepositoryImplTest.this.cacheEvictionService).evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }
}

