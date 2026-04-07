package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectRepositoryImplTest {

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;

    @Mock
    private CacheEvictionService cacheEvictionService;

    @InjectMocks
    private SubjectRepositoryImpl subjectRepository;

    @Test
    void findByTeacherId_shouldReturnSubjectsWhenTeacherHasSubjects() {
        Integer teacherId = 1;
        SubjectEntity entity1 = new SubjectEntity(1, "Math", teacherId, null);
        SubjectEntity entity2 = new SubjectEntity(2, "Science", teacherId, null);
        List<SubjectEntity> entities = Arrays.asList(entity1, entity2);

        Subject subject1 = Subject.builder().id(1).name("Math").teacherId(teacherId).build();
        Subject subject2 = Subject.builder().id(2).name("Science").teacherId(teacherId).build();
        List<Subject> expectedSubjects = Arrays.asList(subject1, subject2);

        when(subjectJPARepository.findByTeacherId(teacherId)).thenReturn(entities);
        when(subjectMapper.toModelList(entities)).thenReturn(expectedSubjects);

        List<Subject> result = subjectRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectJPARepository, times(1)).findByTeacherId(teacherId);
        verify(subjectMapper, times(1)).toModelList(entities);
    }

    @Test
    void findByTeacherId_shouldReturnEmptyListWhenTeacherHasNoSubjects() {
        Integer teacherId = 1;

        when(subjectJPARepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
        when(subjectMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<Subject> result = subjectRepository.findByTeacherId(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subjectJPARepository, times(1)).findByTeacherId(teacherId);
        verify(subjectMapper, times(1)).toModelList(Collections.emptyList());
    }

    @Test
    void save_shouldMapToEntityAndSaveAndMapBackToModel() {
        Subject subjectToSave = Subject.builder().name("History").teacherId(1).build();
        SubjectEntity subjectEntity = new SubjectEntity();
        SubjectEntity savedSubjectEntity = new SubjectEntity(1, "History", 1, null);
        Subject savedSubject = Subject.builder().id(1).name("History").teacherId(1).build();

        when(subjectMapper.toEntity(subjectToSave)).thenReturn(subjectEntity);
        when(subjectJPARepository.save(subjectEntity)).thenReturn(savedSubjectEntity);
        when(subjectMapper.toModel(savedSubjectEntity)).thenReturn(savedSubject);
        when(subjectClassJPARepository.findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(1))
                .thenReturn(Arrays.asList(10, 20));

        Subject result = subjectRepository.save(subjectToSave);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("History", result.getName());
        verify(subjectMapper, times(1)).toEntity(subjectToSave);
        verify(subjectJPARepository, times(1)).save(subjectEntity);
        verify(subjectMapper, times(1)).toModel(savedSubjectEntity);
        verify(cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS, 10);
        verify(cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS, 20);
        verify(cacheEvictionService).evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
    }

    @Test
    void findById_shouldReturnSubjectWhenFoundAndNotDeleted() {
        Integer subjectId = 1;
        SubjectEntity subjectEntity = new SubjectEntity(subjectId, "Math", 1, null);
        Subject expectedSubject = Subject.builder().id(subjectId).name("Math").teacherId(1).build();

        when(subjectJPARepository.findByIdAndDeletionDateIsNull(subjectId)).thenReturn(Optional.of(subjectEntity));
        when(subjectMapper.toModel(subjectEntity)).thenReturn(expectedSubject);

        Optional<Subject> result = subjectRepository.findById(subjectId);

        assertTrue(result.isPresent());
        assertEquals(expectedSubject, result.get());
        verify(subjectJPARepository, times(1)).findByIdAndDeletionDateIsNull(subjectId);
        verify(subjectMapper, times(1)).toModel(subjectEntity);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Integer subjectId = 1;

        when(subjectJPARepository.findByIdAndDeletionDateIsNull(subjectId)).thenReturn(Optional.empty());

        Optional<Subject> result = subjectRepository.findById(subjectId);

        assertFalse(result.isPresent());
        verify(subjectJPARepository, times(1)).findByIdAndDeletionDateIsNull(subjectId);
        verify(subjectMapper, never()).toModel(any(SubjectEntity.class));
    }

    @Test
    void findByIdAndTeacherId_shouldReturnSubjectWhenFoundAndOwnedByTeacher() {
        Integer subjectId = 1;
        Integer teacherId = 101;
        SubjectEntity subjectEntity = new SubjectEntity(subjectId, "Physics", teacherId, null);
        Subject expectedSubject = Subject.builder().id(subjectId).name("Physics").teacherId(teacherId).build();

        when(subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)).thenReturn(Optional.of(subjectEntity));
        when(subjectMapper.toModel(subjectEntity)).thenReturn(expectedSubject);

        Optional<Subject> result = subjectRepository.findByIdAndTeacherId(subjectId, teacherId);

        assertTrue(result.isPresent());
        assertEquals(expectedSubject, result.get());
        verify(subjectJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
        verify(subjectMapper, times(1)).toModel(subjectEntity);
    }

    @Test
    void findByIdAndTeacherId_shouldReturnEmptyWhenNotFoundOrNotOwnedByTeacher() {
        Integer subjectId = 1;
        Integer teacherId = 101;

        when(subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)).thenReturn(Optional.empty());

        Optional<Subject> result = subjectRepository.findByIdAndTeacherId(subjectId, teacherId);

        assertFalse(result.isPresent());
        verify(subjectJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
        verify(subjectMapper, never()).toModel(any(SubjectEntity.class));
    }

    @Test
    void softDeleteSubject_shouldSetDeletionDateAndReturnUpdatedSubject() {
        Integer subjectId = 1;
        Integer teacherId = 101;
        SubjectEntity subjectEntity = new SubjectEntity(subjectId, "Chemistry", teacherId, null);
        Subject updatedSubject = Subject.builder().id(subjectId).name("Chemistry").teacherId(teacherId).deletionDate(LocalDate.now()).build();

        when(subjectClassJPARepository.findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId))
                .thenReturn(Arrays.asList(10));
        when(subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)).thenReturn(Optional.of(subjectEntity));
        when(subjectJPARepository.save(any(SubjectEntity.class))).thenReturn(subjectEntity);
        when(subjectMapper.toModel(any(SubjectEntity.class))).thenReturn(updatedSubject);

        Subject result = subjectRepository.softDeleteSubject(subjectId, teacherId);

        assertNotNull(result);
        assertEquals(subjectId, result.getId());
        assertEquals(teacherId, result.getTeacherId());
        assertNotNull(result.getDeletionDate());
        verify(subjectJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
        verify(subjectJPARepository, times(1)).save(subjectEntity);
        verify(subjectMapper, times(1)).toModel(subjectEntity);
        verify(cacheEvictionService).evict(CacheName.SUBJECT_CLASSES_BY_CLASS, 10);
        verify(cacheEvictionService).evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
    }

    @Test
    void softDeleteSubject_shouldThrowExceptionWhenSubjectNotFoundOrNotOwned() {
        Integer subjectId = 1;
        Integer teacherId = 101;

        when(subjectClassJPARepository.findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId))
                .thenReturn(Collections.emptyList());
        when(subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> subjectRepository.softDeleteSubject(subjectId, teacherId));
        verify(subjectJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
        verify(subjectJPARepository, never()).save(any(SubjectEntity.class));
        verify(subjectMapper, never()).toModel(any(SubjectEntity.class));
    }

    @Test
    void softDeleteSubject_shouldThrowExceptionWhenSubjectAlreadyDeleted() {
        Integer subjectId = 1;
        Integer teacherId = 101;

        when(subjectClassJPARepository.findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId))
                .thenReturn(Collections.emptyList());
        when(subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> subjectRepository.softDeleteSubject(subjectId, teacherId));
        verify(subjectJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId);
        verify(subjectJPARepository, never()).save(any(SubjectEntity.class));
    }
}
