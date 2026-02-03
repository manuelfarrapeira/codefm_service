package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassRepositoryImplTest {

    @Mock
    private ClassJPARepository classJPARepository;

    @Mock
    private ClassMapper classMapper;

    @InjectMocks
    private ClassRepositoryImpl classRepository;

    @Test
    void findActiveClassesBySchoolIdAndTeacherId_shouldReturnActiveClasses() {
        Integer schoolId = 1;
        Integer teacherId = 1;
        ClassEntity entity1 = new ClassEntity(1, schoolId, "Math Class", "24/25", null);
        ClassEntity entity2 = new ClassEntity(2, schoolId, "Science Class", "24/25", null);
        List<ClassEntity> entities = Arrays.asList(entity1, entity2);

        Class class1 = Class.builder().id(1).schoolId(schoolId).name("Math Class").schoolYear("24/25").build();
        Class class2 = Class.builder().id(2).schoolId(schoolId).name("Science Class").schoolYear("24/25").build();
        List<Class> expectedClasses = Arrays.asList(class1, class2);

        when(classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(entities);
        when(classMapper.toModelList(entities)).thenReturn(expectedClasses);

        List<Class> result = classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(classJPARepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        verify(classMapper, times(1)).toModelList(entities);
    }

    @Test
    void findActiveClassesBySchoolIdAndTeacherId_shouldReturnEmptyList_whenNoClassesFound() {
        Integer schoolId = 1;
        Integer teacherId = 1;
        List<ClassEntity> entities = List.of();

        when(classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(entities);
        when(classMapper.toModelList(entities)).thenReturn(List.of());

        List<Class> result = classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(classJPARepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        verify(classMapper, times(1)).toModelList(entities);
    }

    @Test
    void save_shouldSaveClass() {
        Integer schoolId = 1;
        Class classToSave = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        ClassEntity entityToSave = new ClassEntity(null, schoolId, "Math Class", "24/25", null);
        ClassEntity savedEntity = new ClassEntity(1, schoolId, "Math Class", "24/25", null);

        Class expectedClass = Class.builder()
                .id(1)
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(classMapper.toEntity(classToSave)).thenReturn(entityToSave);
        when(classJPARepository.save(entityToSave)).thenReturn(savedEntity);
        when(classMapper.toModel(savedEntity)).thenReturn(expectedClass);

        Class result = classRepository.save(classToSave);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Math Class", result.getName());
        assertEquals("24/25", result.getSchoolYear());
        verify(classMapper, times(1)).toEntity(classToSave);
        verify(classJPARepository, times(1)).save(entityToSave);
        verify(classMapper, times(1)).toModel(savedEntity);
    }

    @Test
    void findById_shouldReturnClass_whenClassExists() {
        Integer classId = 1;
        ClassEntity entity = new ClassEntity(classId, 1, "Math Class", "24/25", null);
        Class expectedClass = Class.builder()
                .id(classId)
                .schoolId(1)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(classJPARepository.findByIdAndDeletionDateIsNull(classId)).thenReturn(Optional.of(entity));
        when(classMapper.toModel(entity)).thenReturn(expectedClass);

        Optional<Class> result = classRepository.findById(classId);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(classId, result.get().getId());
        verify(classJPARepository, times(1)).findByIdAndDeletionDateIsNull(classId);
        verify(classMapper, times(1)).toModel(entity);
    }

    @Test
    void findById_shouldReturnEmpty_whenClassDoesNotExist() {
        Integer classId = 999;

        when(classJPARepository.findByIdAndDeletionDateIsNull(classId)).thenReturn(Optional.empty());

        Optional<Class> result = classRepository.findById(classId);

        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(classJPARepository, times(1)).findByIdAndDeletionDateIsNull(classId);
        verify(classMapper, never()).toModel(any());
    }

    @Test
    void findById_shouldReturnEmpty_whenClassIsDeleted() {
        Integer classId = 1;

        when(classJPARepository.findByIdAndDeletionDateIsNull(classId)).thenReturn(Optional.empty());

        Optional<Class> result = classRepository.findById(classId);

        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(classJPARepository, times(1)).findByIdAndDeletionDateIsNull(classId);
        verify(classMapper, never()).toModel(any());
    }

    @Test
    void findByIdAndTeacherIdAndDeletionDateIsNull_shouldReturnClass_whenClassExists() {
        Integer classId = 1;
        Integer teacherId = 1;
        ClassEntity entity = new ClassEntity(classId, 1, "Math Class", "24/25", null);
        Class expectedClass = Class.builder()
                .id(classId)
                .schoolId(1)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(entity));
        when(classMapper.toModel(entity)).thenReturn(expectedClass);

        Optional<Class> result = classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(classId, result.get().getId());
        assertEquals(teacherId, result.get().getSchoolId());
        verify(classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(classMapper, times(1)).toModel(entity);
    }

    @Test
    void findByIdAndTeacherIdAndDeletionDateIsNull_shouldReturnEmpty_whenClassDoesNotExist() {
        Integer classId = 999;
        Integer teacherId = 1;

        when(classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.empty());

        Optional<Class> result = classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);

        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(classMapper, never()).toModel(any());
    }

    @Test
    void findByIdAndTeacherIdAndDeletionDateIsNull_shouldReturnEmpty_whenClassNotOwnedByTeacher() {
        Integer classId = 1;
        Integer teacherId = 999;

        when(classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.empty());

        Optional<Class> result = classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);

        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(classMapper, never()).toModel(any());
    }

    @Test
    void findByIdAndTeacherIdAndDeletionDateIsNull_shouldReturnEmpty_whenClassIsDeleted() {
        Integer classId = 1;
        Integer teacherId = 1;

        when(classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.empty());

        Optional<Class> result = classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);

        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(classMapper, never()).toModel(any());
    }

    @Test
    void softDeleteClass_shouldSetDeletionDateAndReturnUpdatedClass() {
        Integer classId = 1;
        Integer teacherId = 1;

        ClassEntity classEntity = new ClassEntity();
        classEntity.setId(classId);
        classEntity.setSchoolId(1);
        classEntity.setName("Test Class");
        classEntity.setSchoolYear("24/25");
        classEntity.setDeletionDate(null);

        ClassEntity updatedEntity = new ClassEntity();
        updatedEntity.setId(classId);
        updatedEntity.setSchoolId(1);
        updatedEntity.setName("Test Class");
        updatedEntity.setSchoolYear("24/25");
        updatedEntity.setDeletionDate(LocalDate.now());

        Class expectedClass = Class.builder()
                .id(classId)
                .schoolId(1)
                .name("Test Class")
                .schoolYear("24/25")
                .build();

        when(classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(classEntity));
        when(classJPARepository.save(any(ClassEntity.class))).thenReturn(updatedEntity);
        when(classMapper.toModel(updatedEntity)).thenReturn(expectedClass);

        Class result = classRepository.softDeleteClass(classId, teacherId);

        assertNotNull(result);
        verify(classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(classJPARepository, times(1)).save(any(ClassEntity.class));
        verify(classMapper, times(1)).toModel(updatedEntity);
    }

    @Test
    void softDeleteClass_shouldThrowException_whenClassNotFoundOrNotOwned() {
        Integer classId = 999;
        Integer teacherId = 1;

        when(classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> classRepository.softDeleteClass(classId, teacherId));

        verify(classJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(classJPARepository, never()).save(any());
    }
}

