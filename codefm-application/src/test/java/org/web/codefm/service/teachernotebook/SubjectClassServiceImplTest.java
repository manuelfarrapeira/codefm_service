package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SubjectClassValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectClassServiceImplTest {

    @Mock
    private SubjectClassRepository subjectClassRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private SubjectClassServiceImpl subjectClassService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID_1 = 100;
    private static final Integer SUBJECT_ID_2 = 101;

    @Test
    void getSubjectsByClassId_shouldReturnSubjects_whenClassBelongsToTeacher() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        List<SubjectClassDetail> expectedSubjects = Arrays.asList(
                SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build()
        );

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(clazz));
        when(subjectClassRepository.findSubjectsByClassId(CLASS_ID)).thenReturn(expectedSubjects);

        List<SubjectClassDetail> result = subjectClassService.getSubjectsByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
        verify(subjectClassRepository).findSubjectsByClassId(CLASS_ID);
    }

    @Test
    void getSubjectsByClassId_shouldThrowNotFoundException_whenClassNotExists() {
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found");

        assertThrows(ClassNotFoundException.class, () -> subjectClassService.getSubjectsByClassId(CLASS_ID));

        verify(subjectClassRepository, never()).findSubjectsByClassId(any());
    }

    @Test
    void getSubjectsByClassId_shouldThrowClassForbidden_whenClassDoesNotBelongToTeacher() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.CLASS_FORBIDDEN), any(), any(Locale.class)))
                .thenReturn("Forbidden");

        assertThrows(ClassForbiddenException.class, () -> subjectClassService.getSubjectsByClassId(CLASS_ID));

        verify(subjectClassRepository, never()).findSubjectsByClassId(any());
    }

    @Test
    void getAllClassesWithSubjects_shouldReturnAllClassesWithSubjects() {
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        List<ClassWithSubjects> expectedResult = List.of(
                ClassWithSubjects.builder()
                        .classData(Class.builder().id(CLASS_ID).name("1A").build())
                        .subjects(List.of(SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build()))
                        .build()
        );

        when(subjectClassRepository.findAllClassesWithSubjectsByTeacherId(TEACHER_ID)).thenReturn(expectedResult);

        List<ClassWithSubjects> result = subjectClassService.getAllClassesWithSubjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(subjectClassRepository).findAllClassesWithSubjectsByTeacherId(TEACHER_ID);
    }

    @Test
    void assignSubjectsToClass_shouldAssignSubjects_whenAllValidationsPass() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
        List<SubjectClassDetail> expectedSubjects = Arrays.asList(
                SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build()
        );

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(clazz));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_1, TEACHER_ID))
                .thenReturn(Optional.of(Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build()));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_2, TEACHER_ID))
                .thenReturn(Optional.of(Subject.builder().id(SUBJECT_ID_2).name("Science").teacherId(TEACHER_ID).build()));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(anyInt(), eq(CLASS_ID)))
                .thenReturn(false);
        when(subjectClassRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(subjectClassRepository.findSubjectsByClassId(CLASS_ID)).thenReturn(expectedSubjects);

        List<SubjectClassDetail> result = subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectClassRepository).saveAll(anyList());
    }

    @Test
    void assignSubjectsToClass_shouldThrowValidationException_whenSubjectIdsEmpty() {
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Subject IDs required");

        assertThrows(SubjectClassValidationException.class,
                () -> subjectClassService.assignSubjectsToClass(CLASS_ID, new ArrayList<>()));

        verify(subjectClassRepository, never()).saveAll(any());
    }

    @Test
    void assignSubjectsToClass_shouldThrowValidationException_whenSubjectIdsNull() {
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Subject IDs required");

        assertThrows(SubjectClassValidationException.class,
                () -> subjectClassService.assignSubjectsToClass(CLASS_ID, null));

        verify(subjectClassRepository, never()).saveAll(any());
    }

    @Test
    void assignSubjectsToClass_shouldThrowValidationException_whenSubjectNotOwnedByTeacher() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        List<Integer> subjectIds = List.of(SUBJECT_ID_1);

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(clazz));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_1, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_NOT_OWNED), any(), any(Locale.class)))
                .thenReturn("Subject not owned");

        assertThrows(SubjectClassValidationException.class,
                () -> subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds));

        verify(subjectClassRepository, never()).saveAll(any());
    }

    @Test
    void assignSubjectsToClass_shouldThrowValidationException_whenSubjectAlreadyAssigned() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        Subject subject = Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build();
        List<Integer> subjectIds = List.of(SUBJECT_ID_1);

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(clazz));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_1, TEACHER_ID))
                .thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(true);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_ALREADY_EXISTS), any(), any(Locale.class)))
                .thenReturn("Already exists");

        assertThrows(SubjectClassValidationException.class,
                () -> subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds));

        verify(subjectClassRepository, never()).saveAll(any());
    }

    @Test
    void removeSubjectsFromClass_shouldCallSoftDelete() {
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
        doNothing().when(subjectClassRepository).softDeleteAll(CLASS_ID, subjectIds);

        assertDoesNotThrow(() -> subjectClassService.removeSubjectsFromClass(CLASS_ID, subjectIds));

        verify(subjectClassRepository).softDeleteAll(CLASS_ID, subjectIds);
    }

    @Test
    void removeSubjectsFromClass_shouldThrowNotFoundException_whenClassNotExists() {
        List<Integer> subjectIds = List.of(SUBJECT_ID_1);

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found");

        assertThrows(ClassNotFoundException.class,
                () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds));

        verify(subjectClassRepository, never()).softDeleteAll(anyInt(), anyList());
    }

    @Test
    void removeSubjectsFromClass_shouldThrowClassForbidden_whenClassDoesNotBelongToTeacher() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        List<Integer> subjectIds = List.of(SUBJECT_ID_1);

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.CLASS_FORBIDDEN), any(), any(Locale.class)))
                .thenReturn("Forbidden");

        assertThrows(ClassForbiddenException.class,
                () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds));

        verify(subjectClassRepository, never()).softDeleteAll(any(), any());
    }

    @Test
    void removeSubjectsFromClass_shouldThrowValidationException_whenSubjectIdsEmpty() {
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Subject IDs required");

        assertThrows(SubjectClassValidationException.class,
                () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, new ArrayList<>()));

        verify(subjectClassRepository, never()).softDeleteAll(any(), any());
    }

    @Test
    void removeSubjectsFromClass_shouldThrowValidationException_whenAssociationDoesNotExist() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        List<Integer> subjectIds = List.of(SUBJECT_ID_1);

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(clazz));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(false);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Subject not assigned to class");

        assertThrows(SubjectClassValidationException.class,
                () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds));

        verify(subjectClassRepository, never()).softDeleteAll(any(), any());
    }

    @Test
    void findActiveSubjectClassIds_shouldReturnIds_whenAssociationsExist() {
        Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);

        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(clazz));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(true);
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_2, CLASS_ID))
                .thenReturn(true);
        when(subjectClassRepository.findIdBySubjectIdAndClassId(SUBJECT_ID_1, CLASS_ID))
                .thenReturn(Optional.of(200));
        when(subjectClassRepository.findIdBySubjectIdAndClassId(SUBJECT_ID_2, CLASS_ID))
                .thenReturn(Optional.of(201));

        List<Integer> result = subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(200));
        assertTrue(result.contains(201));
    }
}

