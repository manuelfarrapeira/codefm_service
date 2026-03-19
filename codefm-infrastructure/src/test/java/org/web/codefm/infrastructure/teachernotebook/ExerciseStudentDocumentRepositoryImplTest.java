package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentDocumentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseStudentDocumentJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseStudentDocumentMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseStudentDocumentRepositoryImplTest {

    @Mock
    private ExerciseStudentDocumentJPARepository exerciseStudentDocumentJPARepository;

    @Mock
    private ExerciseStudentDocumentMapper exerciseStudentDocumentMapper;

    @InjectMocks
    private ExerciseStudentDocumentRepositoryImpl exerciseStudentDocumentRepository;

    private static final Integer GRADE_ID = 10;
    private static final Integer DOCUMENT_ID = 200;

    @Test
    void save_shouldPersistAndReturnMappedEntity() {
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder().gradeId(GRADE_ID).build();
        final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity();
        final ExerciseStudentDocumentEntity saved = new ExerciseStudentDocumentEntity();
        final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).gradeId(GRADE_ID).build();

        when(exerciseStudentDocumentMapper.toEntity(document)).thenReturn(entity);
        when(exerciseStudentDocumentJPARepository.save(entity)).thenReturn(saved);
        when(exerciseStudentDocumentMapper.toModel(saved)).thenReturn(expected);

        final ExerciseStudentDocument result = exerciseStudentDocumentRepository.save(document);

        assertEquals(expected, result);
    }

    @Test
    void update_shouldPersistAndReturnMappedEntity() {
        final ExerciseStudentDocument document = ExerciseStudentDocument.builder().id(DOCUMENT_ID).gradeId(GRADE_ID).build();
        final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity();
        final ExerciseStudentDocumentEntity saved = new ExerciseStudentDocumentEntity();
        final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).gradeId(GRADE_ID).description("updated").build();

        when(exerciseStudentDocumentMapper.toEntity(document)).thenReturn(entity);
        when(exerciseStudentDocumentJPARepository.save(entity)).thenReturn(saved);
        when(exerciseStudentDocumentMapper.toModel(saved)).thenReturn(expected);

        final ExerciseStudentDocument result = exerciseStudentDocumentRepository.update(document);

        assertEquals(expected, result);
    }

    @Test
    void findByGradeId_shouldReturnMappedList() {
        final List<ExerciseStudentDocumentEntity> entities = List.of(new ExerciseStudentDocumentEntity());
        final List<ExerciseStudentDocument> expected = List.of(ExerciseStudentDocument.builder().id(DOCUMENT_ID).build());

        when(exerciseStudentDocumentJPARepository.findByGradeId(GRADE_ID)).thenReturn(entities);
        when(exerciseStudentDocumentMapper.toModelList(entities)).thenReturn(expected);

        final List<ExerciseStudentDocument> result = exerciseStudentDocumentRepository.findByGradeId(GRADE_ID);

        assertEquals(expected, result);
    }

    @Test
    void findById_shouldReturnMappedEntity_whenExists() {
        final ExerciseStudentDocumentEntity entity = new ExerciseStudentDocumentEntity();
        final ExerciseStudentDocument expected = ExerciseStudentDocument.builder().id(DOCUMENT_ID).build();

        when(exerciseStudentDocumentJPARepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(entity));
        when(exerciseStudentDocumentMapper.toModel(entity)).thenReturn(expected);

        final Optional<ExerciseStudentDocument> result = exerciseStudentDocumentRepository.findById(DOCUMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(exerciseStudentDocumentJPARepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        final Optional<ExerciseStudentDocument> result = exerciseStudentDocumentRepository.findById(DOCUMENT_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_shouldCallJpaDeleteById() {
        exerciseStudentDocumentRepository.deleteById(DOCUMENT_ID);

        verify(exerciseStudentDocumentJPARepository).deleteById(DOCUMENT_ID);
    }

    @Test
    void deleteByGradeId_shouldCallJpaDeleteByGradeId() {
        exerciseStudentDocumentRepository.deleteByGradeId(GRADE_ID);

        verify(exerciseStudentDocumentJPARepository).deleteByGradeId(GRADE_ID);
    }

    @Test
    void deleteByGradeIds_shouldCallJpaDeleteByGradeIdIn_whenNotEmpty() {
        final List<Integer> gradeIds = List.of(1, 2, 3);

        exerciseStudentDocumentRepository.deleteByGradeIds(gradeIds);

        verify(exerciseStudentDocumentJPARepository).deleteByGradeIdIn(gradeIds);
    }

    @Test
    void deleteByGradeIds_shouldNotCallJpa_whenListIsEmpty() {
        exerciseStudentDocumentRepository.deleteByGradeIds(List.of());

        verifyNoInteractions(exerciseStudentDocumentJPARepository);
    }
}

