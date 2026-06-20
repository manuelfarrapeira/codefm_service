package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.SubjectMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectRepositoryImplTest {

    private SubjectRepositoryImpl subjectRepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void beforeEach() {
        this.subjectRepository = new SubjectRepositoryImpl(this.subjectJPARepository, this.subjectMapper,
                this.subjectClassJPARepository, this.cacheEvictionService);
    }

    @Nested
    class FindByTeacherId {

        @Test
        void when_teacher_has_subjects_expect_subjects_returned() {
            final Integer teacherId = 1;
            final SubjectEntity entity1 = new SubjectEntity(1, "Math", teacherId, null);
            final SubjectEntity entity2 = new SubjectEntity(2, "Science", teacherId, null);
            final List<SubjectEntity> entities = Arrays.asList(entity1, entity2);
            final Subject subject1 = Subject.builder().id(1).name("Math").teacherId(teacherId).build();
            final Subject subject2 = Subject.builder().id(2).name("Science").teacherId(teacherId).build();
            final List<Subject> expectedSubjects = Arrays.asList(subject1, subject2);
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByTeacherId(teacherId)).thenReturn(entities);
            when(SubjectRepositoryImplTest.this.subjectMapper.toModelList(entities)).thenReturn(expectedSubjects);

            final List<Subject> result = SubjectRepositoryImplTest.this.subjectRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().hasSize(2);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1)).findByTeacherId(teacherId);
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toModelList(entities);
        }

        @Test
        void when_teacher_has_no_subjects_expect_empty_list_returned() {
            final Integer teacherId = 1;
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByTeacherId(teacherId))
                    .thenReturn(Collections.emptyList());
            when(SubjectRepositoryImplTest.this.subjectMapper.toModelList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            final List<Subject> result = SubjectRepositoryImplTest.this.subjectRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1)).findByTeacherId(teacherId);
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toModelList(Collections.emptyList());
        }
    }

    @Nested
    class Save {

        @Test
        void when_subject_is_saved_expect_model_returned() {
            final Subject subjectToSave = Subject.builder().name("History").teacherId(1).build();
            final SubjectEntity subjectEntity = new SubjectEntity();
            final SubjectEntity savedSubjectEntity = new SubjectEntity(1, "History", 1, null);
            final Subject savedSubject = Subject.builder().id(1).name("History").teacherId(1).build();
            when(SubjectRepositoryImplTest.this.subjectMapper.toEntity(subjectToSave)).thenReturn(subjectEntity);
            when(SubjectRepositoryImplTest.this.subjectJPARepository.save(subjectEntity)).thenReturn(savedSubjectEntity);
            when(SubjectRepositoryImplTest.this.subjectMapper.toModel(savedSubjectEntity)).thenReturn(savedSubject);
            when(SubjectRepositoryImplTest.this.subjectClassJPARepository
                    .findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(1)).thenReturn(Arrays.asList(10, 20));

            final Subject result = SubjectRepositoryImplTest.this.subjectRepository.save(subjectToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("History");
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toEntity(subjectToSave);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1)).save(subjectEntity);
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toModel(savedSubjectEntity);
            verify(SubjectRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS, 10);
            verify(SubjectRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS, 20);
            verify(SubjectRepositoryImplTest.this.cacheEvictionService)
                    .evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_subject_exists_expect_subject_returned() {
            final Integer subjectId = 1;
            final SubjectEntity subjectEntity = new SubjectEntity(subjectId, "Math", 1, null);
            final Subject expectedSubject = Subject.builder().id(subjectId).name("Math").teacherId(1).build();
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndDeletionDateIsNull(subjectId))
                    .thenReturn(Optional.of(subjectEntity));
            when(SubjectRepositoryImplTest.this.subjectMapper.toModel(subjectEntity)).thenReturn(expectedSubject);

            final Optional<Subject> result = SubjectRepositoryImplTest.this.subjectRepository.findById(subjectId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedSubject);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndDeletionDateIsNull(subjectId);
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toModel(subjectEntity);
        }

        @Test
        void when_subject_does_not_exist_expect_empty_optional_returned() {
            final Integer subjectId = 1;
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndDeletionDateIsNull(subjectId))
                    .thenReturn(Optional.empty());

            final Optional<Subject> result = SubjectRepositoryImplTest.this.subjectRepository.findById(subjectId);

            assertThat(result).isNotPresent();
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndDeletionDateIsNull(subjectId);
            verify(SubjectRepositoryImplTest.this.subjectMapper, never()).toModel(any(SubjectEntity.class));
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_subject_exists_and_is_owned_expect_subject_returned() {
            final Integer subjectId = 1;
            final Integer teacherId = 101;
            final SubjectEntity subjectEntity = new SubjectEntity(subjectId, "Physics", teacherId, null);
            final Subject expectedSubject = Subject.builder().id(subjectId).name("Physics").teacherId(teacherId)
                    .build();
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(
                    subjectId, teacherId)).thenReturn(Optional.of(subjectEntity));
            when(SubjectRepositoryImplTest.this.subjectMapper.toModel(subjectEntity)).thenReturn(expectedSubject);

            final Optional<Subject> result = SubjectRepositoryImplTest.this.subjectRepository.findByIdAndTeacherId(
                    subjectId, teacherId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedSubject);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toModel(subjectEntity);
        }

        @Test
        void when_subject_does_not_exist_or_is_not_owned_expect_empty_optional_returned() {
            final Integer subjectId = 1;
            final Integer teacherId = 101;
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(
                    subjectId, teacherId)).thenReturn(Optional.empty());

            final Optional<Subject> result = SubjectRepositoryImplTest.this.subjectRepository.findByIdAndTeacherId(
                    subjectId, teacherId);

            assertThat(result).isNotPresent();
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
            verify(SubjectRepositoryImplTest.this.subjectMapper, never()).toModel(any(SubjectEntity.class));
        }
    }

    @Nested
    class SoftDeleteSubject {

        @Test
        void when_subject_exists_expect_deletion_date_set_and_subject_returned() {
            final Integer subjectId = 1;
            final Integer teacherId = 101;
            final SubjectEntity subjectEntity = new SubjectEntity(subjectId, "Chemistry", teacherId, null);
            final Subject updatedSubject = Subject.builder().id(subjectId).name("Chemistry").teacherId(teacherId)
                    .deletionDate(LocalDate.now()).build();
            when(SubjectRepositoryImplTest.this.subjectClassJPARepository
                    .findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId)).thenReturn(Arrays.asList(10));
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(
                    subjectId, teacherId)).thenReturn(Optional.of(subjectEntity));
            when(SubjectRepositoryImplTest.this.subjectJPARepository.save(any(SubjectEntity.class)))
                    .thenReturn(subjectEntity);
            when(SubjectRepositoryImplTest.this.subjectMapper.toModel(any(SubjectEntity.class)))
                    .thenReturn(updatedSubject);

            final Subject result = SubjectRepositoryImplTest.this.subjectRepository.softDeleteSubject(subjectId,
                    teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subjectId);
            assertThat(result.getTeacherId()).isEqualTo(teacherId);
            assertThat(result.getDeletionDate()).isNotNull();
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1)).save(subjectEntity);
            verify(SubjectRepositoryImplTest.this.subjectMapper, times(1)).toModel(subjectEntity);
            verify(SubjectRepositoryImplTest.this.cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS, 10);
            verify(SubjectRepositoryImplTest.this.cacheEvictionService)
                    .evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        }

        @Test
        void when_subject_is_not_found_or_not_owned_expect_exception_thrown() {
            final Integer subjectId = 1;
            final Integer teacherId = 101;
            when(SubjectRepositoryImplTest.this.subjectClassJPARepository
                    .findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId))
                    .thenReturn(Collections.emptyList());
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(
                    subjectId, teacherId)).thenReturn(Optional.empty());
            final ThrowingCallable callable = () -> SubjectRepositoryImplTest.this.subjectRepository
                    .softDeleteSubject(subjectId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, never()).save(any(SubjectEntity.class));
            verify(SubjectRepositoryImplTest.this.subjectMapper, never()).toModel(any(SubjectEntity.class));
        }

        @Test
        void when_subject_is_already_deleted_expect_exception_thrown() {
            final Integer subjectId = 1;
            final Integer teacherId = 101;
            when(SubjectRepositoryImplTest.this.subjectClassJPARepository
                    .findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId))
                    .thenReturn(Collections.emptyList());
            when(SubjectRepositoryImplTest.this.subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(
                    subjectId, teacherId)).thenReturn(Optional.empty());
            final ThrowingCallable callable = () -> SubjectRepositoryImplTest.this.subjectRepository
                    .softDeleteSubject(subjectId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, times(1))
                    .findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
            verify(SubjectRepositoryImplTest.this.subjectJPARepository, never()).save(any(SubjectEntity.class));
        }
    }
}
