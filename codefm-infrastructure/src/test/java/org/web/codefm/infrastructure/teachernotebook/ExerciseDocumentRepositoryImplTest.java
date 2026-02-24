package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseDocumentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseDocumentJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseDocumentMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseDocumentRepositoryImplTest {

    @Mock
    private ExerciseDocumentJPARepository exerciseDocumentJPARepository;

    @Mock
    private ExerciseDocumentMapper exerciseDocumentMapper;

    @InjectMocks
    private ExerciseDocumentRepositoryImpl exerciseDocumentRepository;

    @Test
    void save_shouldMapAndSaveEntity() {
        ExerciseDocument input = ExerciseDocument.builder()
                .exerciseId(1).document("file.pdf").description("desc").build();
        ExerciseDocumentEntity entity = new ExerciseDocumentEntity(null, 1, "file.pdf", "desc");
        ExerciseDocumentEntity savedEntity = new ExerciseDocumentEntity(10, 1, "file.pdf", "desc");
        ExerciseDocument expected = ExerciseDocument.builder()
                .id(10).exerciseId(1).document("file.pdf").description("desc").build();

        when(exerciseDocumentMapper.toEntity(input)).thenReturn(entity);
        when(exerciseDocumentJPARepository.save(entity)).thenReturn(savedEntity);
        when(exerciseDocumentMapper.toModel(savedEntity)).thenReturn(expected);

        ExerciseDocument result = exerciseDocumentRepository.save(input);

        assertNotNull(result);
        assertEquals(10, result.getId());
        verify(exerciseDocumentJPARepository).save(entity);
    }

    @Test
    void update_shouldMapAndUpdateEntity() {
        ExerciseDocument input = ExerciseDocument.builder()
                .id(10).exerciseId(1).document("file.pdf").description("updated").build();
        ExerciseDocumentEntity entity = new ExerciseDocumentEntity(10, 1, "file.pdf", "updated");
        ExerciseDocument expected = ExerciseDocument.builder()
                .id(10).exerciseId(1).document("file.pdf").description("updated").build();

        when(exerciseDocumentMapper.toEntity(input)).thenReturn(entity);
        when(exerciseDocumentJPARepository.save(entity)).thenReturn(entity);
        when(exerciseDocumentMapper.toModel(entity)).thenReturn(expected);

        ExerciseDocument result = exerciseDocumentRepository.update(input);

        assertNotNull(result);
        assertEquals("updated", result.getDescription());
        verify(exerciseDocumentJPARepository).save(entity);
    }

    @Test
    void findByExerciseId_shouldReturnMappedDocuments() {
        Integer exerciseId = 1;
        List<ExerciseDocumentEntity> entities = List.of(
                new ExerciseDocumentEntity(10, exerciseId, "file1.pdf", "desc1"),
                new ExerciseDocumentEntity(11, exerciseId, "file2.pdf", "desc2")
        );
        List<ExerciseDocument> expected = List.of(
                ExerciseDocument.builder().id(10).exerciseId(exerciseId).document("file1.pdf").build(),
                ExerciseDocument.builder().id(11).exerciseId(exerciseId).document("file2.pdf").build()
        );

        when(exerciseDocumentJPARepository.findByExerciseId(exerciseId)).thenReturn(entities);
        when(exerciseDocumentMapper.toModelList(entities)).thenReturn(expected);

        List<ExerciseDocument> result = exerciseDocumentRepository.findByExerciseId(exerciseId);

        assertEquals(2, result.size());
        verify(exerciseDocumentJPARepository).findByExerciseId(exerciseId);
    }

    @Test
    void findById_shouldReturnMappedDocument_whenFound() {
        ExerciseDocumentEntity entity = new ExerciseDocumentEntity(10, 1, "file.pdf", "desc");
        ExerciseDocument expected = ExerciseDocument.builder()
                .id(10).exerciseId(1).document("file.pdf").description("desc").build();

        when(exerciseDocumentJPARepository.findById(10)).thenReturn(Optional.of(entity));
        when(exerciseDocumentMapper.toModel(entity)).thenReturn(expected);

        Optional<ExerciseDocument> result = exerciseDocumentRepository.findById(10);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(exerciseDocumentJPARepository.findById(10)).thenReturn(Optional.empty());

        Optional<ExerciseDocument> result = exerciseDocumentRepository.findById(10);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_shouldDelegateToJPARepository() {
        exerciseDocumentRepository.deleteById(10);

        verify(exerciseDocumentJPARepository).deleteById(10);
    }

    @Test
    void deleteByExerciseId_shouldDelegateToJPARepository() {
        exerciseDocumentRepository.deleteByExerciseId(1);

        verify(exerciseDocumentJPARepository).deleteByExerciseId(1);
    }

    @Test
    void deleteByExerciseIds_shouldDelegateToJPARepository() {
        List<Integer> exerciseIds = List.of(1, 2, 3);

        exerciseDocumentRepository.deleteByExerciseIds(exerciseIds);

        verify(exerciseDocumentJPARepository).deleteByExerciseIdIn(exerciseIds);
    }

    @Test
    void deleteByExerciseIds_shouldDoNothing_whenListIsNull() {
        exerciseDocumentRepository.deleteByExerciseIds(null);

        verify(exerciseDocumentJPARepository, never()).deleteByExerciseIdIn(any());
    }

    @Test
    void deleteByExerciseIds_shouldDoNothing_whenListIsEmpty() {
        exerciseDocumentRepository.deleteByExerciseIds(List.of());

        verify(exerciseDocumentJPARepository, never()).deleteByExerciseIdIn(any());
    }

    @Test
    void findByExerciseIds_shouldReturnMappedDocuments() {
        List<Integer> exerciseIds = List.of(1, 2);
        List<ExerciseDocumentEntity> entities = List.of(
                new ExerciseDocumentEntity(10, 1, "file1.pdf", "desc1"),
                new ExerciseDocumentEntity(11, 2, "file2.pdf", "desc2")
        );
        List<ExerciseDocument> expected = List.of(
                ExerciseDocument.builder().id(10).exerciseId(1).document("file1.pdf").build(),
                ExerciseDocument.builder().id(11).exerciseId(2).document("file2.pdf").build()
        );

        when(exerciseDocumentJPARepository.findByExerciseIdIn(exerciseIds)).thenReturn(entities);
        when(exerciseDocumentMapper.toModelList(entities)).thenReturn(expected);

        List<ExerciseDocument> result = exerciseDocumentRepository.findByExerciseIds(exerciseIds);

        assertEquals(2, result.size());
    }

    @Test
    void findByExerciseIds_shouldReturnEmptyList_whenListIsNull() {
        List<ExerciseDocument> result = exerciseDocumentRepository.findByExerciseIds(null);

        assertTrue(result.isEmpty());
        verify(exerciseDocumentJPARepository, never()).findByExerciseIdIn(any());
    }

    @Test
    void findByExerciseIds_shouldReturnEmptyList_whenListIsEmpty() {
        List<ExerciseDocument> result = exerciseDocumentRepository.findByExerciseIds(List.of());

        assertTrue(result.isEmpty());
        verify(exerciseDocumentJPARepository, never()).findByExerciseIdIn(any());
    }
}

