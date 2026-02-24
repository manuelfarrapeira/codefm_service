package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;
import org.web.codefm.infrastructure.mapper.SubjectClassMapper;
import org.web.codefm.infrastructure.mapper.SubjectMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectClassRepositoryImplTest {

    @Mock
    private SubjectClassJPARepository subjectClassJPARepository;
    @Mock
    private SubjectJPARepository subjectJPARepository;
    @Mock
    private ClassJPARepository classJPARepository;
    @Mock
    private SubjectClassMapper subjectClassMapper;
    @Mock
    private SubjectMapper subjectMapper;
    @Mock
    private ClassMapper classMapper;

    @InjectMocks
    private SubjectClassRepositoryImpl subjectClassRepository;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID_1 = 100;
    private static final Integer SUBJECT_ID_2 = 101;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subjectClassRepository = new SubjectClassRepositoryImpl(
                subjectClassJPARepository, subjectJPARepository, classJPARepository,
                subjectClassMapper, subjectMapper, classMapper);
    }

    @Test
    void findSubjectsByClassId_shouldReturnSubjects_whenAssociationsExist() {
        SubjectClassEntity scEntity1 = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
        SubjectClassEntity scEntity2 = new SubjectClassEntity(2, SUBJECT_ID_2, CLASS_ID, null);
        List<SubjectClassEntity> scEntities = Arrays.asList(scEntity1, scEntity2);

        SubjectEntity subjectEntity1 = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
        SubjectEntity subjectEntity2 = new SubjectEntity(SUBJECT_ID_2, "Science", TEACHER_ID, null);
        List<SubjectEntity> subjectEntities = Arrays.asList(subjectEntity1, subjectEntity2);

        Subject subject1 = Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build();
        Subject subject2 = Subject.builder().id(SUBJECT_ID_2).name("Science").teacherId(TEACHER_ID).build();
        List<Subject> expectedSubjects = Arrays.asList(subject1, subject2);

        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(scEntities);
        when(subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2))).thenReturn(subjectEntities);
        when(subjectMapper.toModelList(subjectEntities)).thenReturn(expectedSubjects);

        List<Subject> result = subjectClassRepository.findSubjectsByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectClassJPARepository).findByClassIdAndDeletionDateIsNull(CLASS_ID);
    }

    @Test
    void findSubjectsByClassId_shouldReturnEmptyList_whenNoAssociationsExist() {
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(Arrays.asList());

        List<Subject> result = subjectClassRepository.findSubjectsByClassId(CLASS_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveAll_shouldSaveAndReturnSubjectClasses() {
        SubjectClass sc1 = SubjectClass.builder().subjectId(SUBJECT_ID_1).classId(CLASS_ID).build();
        SubjectClass sc2 = SubjectClass.builder().subjectId(SUBJECT_ID_2).classId(CLASS_ID).build();
        List<SubjectClass> subjectClasses = Arrays.asList(sc1, sc2);

        SubjectClassEntity scEntity1 = new SubjectClassEntity(null, SUBJECT_ID_1, CLASS_ID, null);
        SubjectClassEntity scEntity2 = new SubjectClassEntity(null, SUBJECT_ID_2, CLASS_ID, null);
        List<SubjectClassEntity> entities = Arrays.asList(scEntity1, scEntity2);

        SubjectClassEntity savedEntity1 = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
        SubjectClassEntity savedEntity2 = new SubjectClassEntity(2, SUBJECT_ID_2, CLASS_ID, null);
        List<SubjectClassEntity> savedEntities = Arrays.asList(savedEntity1, savedEntity2);

        SubjectClass savedSc1 = SubjectClass.builder().id(1).subjectId(SUBJECT_ID_1).classId(CLASS_ID).build();
        SubjectClass savedSc2 = SubjectClass.builder().id(2).subjectId(SUBJECT_ID_2).classId(CLASS_ID).build();
        List<SubjectClass> expectedResult = Arrays.asList(savedSc1, savedSc2);

        when(subjectClassMapper.toEntityList(subjectClasses)).thenReturn(entities);
        when(subjectClassJPARepository.saveAll(entities)).thenReturn(savedEntities);
        when(subjectClassMapper.toModelList(savedEntities)).thenReturn(expectedResult);

        List<SubjectClass> result = subjectClassRepository.saveAll(subjectClasses);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectClassJPARepository).saveAll(entities);
    }

    @Test
    void softDeleteAll_shouldCallJpaRepository() {
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);

        doNothing().when(subjectClassJPARepository).softDeleteByClassIdAndSubjectIds(CLASS_ID, subjectIds);

        assertDoesNotThrow(() -> subjectClassRepository.softDeleteAll(CLASS_ID, subjectIds));

        verify(subjectClassJPARepository).softDeleteByClassIdAndSubjectIds(CLASS_ID, subjectIds);
    }

    @Test
    void existsBySubjectIdAndClassIdAndDeletionDateIsNull_shouldReturnTrue_whenExists() {
        SubjectClassEntity entity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);

        when(subjectClassJPARepository.findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(Optional.of(entity));

        boolean result = subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID);

        assertTrue(result);
    }

    @Test
    void existsBySubjectIdAndClassIdAndDeletionDateIsNull_shouldReturnFalse_whenNotExists() {
        when(subjectClassJPARepository.findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(Optional.empty());

        boolean result = subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID);

        assertFalse(result);
    }

    @Test
    void findAllClassesWithSubjectsByTeacherId_shouldReturnClassesWithSubjects() {
        List<Integer> classIds = Arrays.asList(CLASS_ID);
        ClassEntity classEntity = new ClassEntity(CLASS_ID, 1, "1A", "24/25", null);
        List<ClassEntity> classEntities = Arrays.asList(classEntity);

        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").schoolYear("24/25").build();

        SubjectClassEntity scEntity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
        SubjectEntity subjectEntity = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
        Subject subject = Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build();

        when(subjectClassJPARepository.findClassIdsByTeacherId(TEACHER_ID)).thenReturn(classIds);
        when(classJPARepository.findAllById(classIds)).thenReturn(classEntities);
        when(classMapper.toModel(classEntity)).thenReturn(clazz);
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID))
                .thenReturn(Arrays.asList(scEntity));
        when(subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1)))
                .thenReturn(Arrays.asList(subjectEntity));
        when(subjectMapper.toModelList(Arrays.asList(subjectEntity)))
                .thenReturn(Arrays.asList(subject));

        List<ClassWithSubjects> result = subjectClassRepository.findAllClassesWithSubjectsByTeacherId(TEACHER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CLASS_ID, result.get(0).getClassData().getId());
        assertEquals(1, result.get(0).getSubjects().size());
    }

    @Test
    void findAllClassesWithSubjectsByTeacherId_shouldReturnEmptyList_whenNoClasses() {
        when(subjectClassJPARepository.findClassIdsByTeacherId(TEACHER_ID)).thenReturn(Arrays.asList());

        List<ClassWithSubjects> result = subjectClassRepository.findAllClassesWithSubjectsByTeacherId(TEACHER_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void softDeleteByClassId_shouldCallJpaRepository() {
        doNothing().when(subjectClassJPARepository).softDeleteByClassId(CLASS_ID);

        subjectClassRepository.softDeleteByClassId(CLASS_ID);

        verify(subjectClassJPARepository).softDeleteByClassId(CLASS_ID);
    }

    @Test
    void softDeleteBySubjectId_shouldCallJpaRepository() {
        doNothing().when(subjectClassJPARepository).softDeleteBySubjectId(SUBJECT_ID_1);

        subjectClassRepository.softDeleteBySubjectId(SUBJECT_ID_1);

        verify(subjectClassJPARepository).softDeleteBySubjectId(SUBJECT_ID_1);
    }

    @Test
    void findActiveIdsByClassId_shouldReturnIds() {
        List<Integer> expectedIds = Arrays.asList(1, 2, 3);
        when(subjectClassJPARepository.findIdsByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(expectedIds);

        List<Integer> result = subjectClassRepository.findActiveIdsByClassId(CLASS_ID);

        assertEquals(3, result.size());
        assertEquals(expectedIds, result);
        verify(subjectClassJPARepository).findIdsByClassIdAndDeletionDateIsNull(CLASS_ID);
    }

    @Test
    void findActiveIdsByClassId_shouldReturnEmptyList_whenNoActiveIds() {
        when(subjectClassJPARepository.findIdsByClassIdAndDeletionDateIsNull(CLASS_ID)).thenReturn(Collections.emptyList());

        List<Integer> result = subjectClassRepository.findActiveIdsByClassId(CLASS_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveIdsBySubjectId_shouldReturnIds() {
        List<Integer> expectedIds = Arrays.asList(5, 6);
        when(subjectClassJPARepository.findIdsBySubjectIdAndDeletionDateIsNull(SUBJECT_ID_1)).thenReturn(expectedIds);

        List<Integer> result = subjectClassRepository.findActiveIdsBySubjectId(SUBJECT_ID_1);

        assertEquals(2, result.size());
        assertEquals(expectedIds, result);
        verify(subjectClassJPARepository).findIdsBySubjectIdAndDeletionDateIsNull(SUBJECT_ID_1);
    }

    @Test
    void findActiveIdsBySubjectId_shouldReturnEmptyList_whenNoActiveIds() {
        when(subjectClassJPARepository.findIdsBySubjectIdAndDeletionDateIsNull(SUBJECT_ID_1)).thenReturn(Collections.emptyList());

        List<Integer> result = subjectClassRepository.findActiveIdsBySubjectId(SUBJECT_ID_1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findIdBySubjectIdAndClassId_shouldReturnId_whenAssociationExists() {
        SubjectClassEntity entity = new SubjectClassEntity(42, SUBJECT_ID_1, CLASS_ID, null);
        when(subjectClassJPARepository.findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(Optional.of(entity));

        Optional<Integer> result = subjectClassRepository.findIdBySubjectIdAndClassId(SUBJECT_ID_1, CLASS_ID);

        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void findIdBySubjectIdAndClassId_shouldReturnEmpty_whenAssociationNotExists() {
        when(subjectClassJPARepository.findBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(Optional.empty());

        Optional<Integer> result = subjectClassRepository.findIdBySubjectIdAndClassId(SUBJECT_ID_1, CLASS_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnSubjectClass_whenExists() {
        SubjectClassEntity entity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
        SubjectClass expectedModel = SubjectClass.builder().id(1).subjectId(SUBJECT_ID_1).classId(CLASS_ID).build();

        when(subjectClassJPARepository.findById(1)).thenReturn(Optional.of(entity));
        when(subjectClassMapper.toModel(entity)).thenReturn(expectedModel);

        Optional<SubjectClass> result = subjectClassRepository.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(SUBJECT_ID_1, result.get().getSubjectId());
        assertEquals(CLASS_ID, result.get().getClassId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(subjectClassJPARepository.findById(999)).thenReturn(Optional.empty());

        Optional<SubjectClass> result = subjectClassRepository.findById(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void findSubjectsByClassId_shouldFilterOutDeletedSubjects() {
        SubjectClassEntity scEntity1 = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
        SubjectClassEntity scEntity2 = new SubjectClassEntity(2, SUBJECT_ID_2, CLASS_ID, null);

        SubjectEntity activeSubject = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
        SubjectEntity deletedSubject = new SubjectEntity(SUBJECT_ID_2, "Science", TEACHER_ID, LocalDate.now());

        Subject subject = Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build();

        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID))
                .thenReturn(Arrays.asList(scEntity1, scEntity2));
        when(subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2)))
                .thenReturn(Arrays.asList(activeSubject, deletedSubject));
        when(subjectMapper.toModelList(List.of(activeSubject)))
                .thenReturn(List.of(subject));

        List<Subject> result = subjectClassRepository.findSubjectsByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Math", result.get(0).getName());
    }

    @Test
    void findAllClassesWithSubjectsByTeacherId_shouldFilterOutDeletedClasses() {
        List<Integer> classIds = Arrays.asList(CLASS_ID, 20);
        ClassEntity activeClass = new ClassEntity(CLASS_ID, 1, "1A", "24/25", null);
        ClassEntity deletedClass = new ClassEntity(20, 1, "2B", "24/25", LocalDate.now());

        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").schoolYear("24/25").build();

        SubjectClassEntity scEntity = new SubjectClassEntity(1, SUBJECT_ID_1, CLASS_ID, null);
        SubjectEntity subjectEntity = new SubjectEntity(SUBJECT_ID_1, "Math", TEACHER_ID, null);
        Subject subject = Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build();

        when(subjectClassJPARepository.findClassIdsByTeacherId(TEACHER_ID)).thenReturn(classIds);
        when(classJPARepository.findAllById(classIds)).thenReturn(Arrays.asList(activeClass, deletedClass));
        when(classMapper.toModel(activeClass)).thenReturn(clazz);
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(CLASS_ID))
                .thenReturn(Arrays.asList(scEntity));
        when(subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(20))
                .thenReturn(Collections.emptyList());
        when(subjectJPARepository.findAllById(Arrays.asList(SUBJECT_ID_1)))
                .thenReturn(Arrays.asList(subjectEntity));
        when(subjectMapper.toModelList(Arrays.asList(subjectEntity)))
                .thenReturn(Arrays.asList(subject));

        List<ClassWithSubjects> result = subjectClassRepository.findAllClassesWithSubjectsByTeacherId(TEACHER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CLASS_ID, result.get(0).getClassData().getId());
    }
}
