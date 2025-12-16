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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        // Given
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

        // When
        List<Class> result = classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(classJPARepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        verify(classMapper, times(1)).toModelList(entities);
    }

    @Test
    void findActiveClassesBySchoolIdAndTeacherId_shouldReturnEmptyList_whenNoClassesFound() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        List<ClassEntity> entities = Arrays.asList();

        when(classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(entities);
        when(classMapper.toModelList(entities)).thenReturn(Arrays.asList());

        // When
        List<Class> result = classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(classJPARepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        verify(classMapper, times(1)).toModelList(entities);
    }
}

