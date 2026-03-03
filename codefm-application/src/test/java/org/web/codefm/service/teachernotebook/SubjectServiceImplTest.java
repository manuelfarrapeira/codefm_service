package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.exception.teachernotebook.SubjectForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SubjectNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SubjectValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceImplTest {

    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private static final Integer TEACHER_ID = 1;

    @Test
    void getSubjectsByTeacher_shouldReturnSubjects_whenFound() {
        List<Subject> expectedSubjects = Arrays.asList(
                Subject.builder().id(1).name("Mathematics").teacherId(TEACHER_ID).build(),
                Subject.builder().id(2).name("Science").teacherId(TEACHER_ID).build()
        );

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(subjectRepository.findByTeacherId(TEACHER_ID)).thenReturn(expectedSubjects);

        List<Subject> actualSubjects = subjectService.getSubjectsByTeacher();

        assertNotNull(actualSubjects);
        assertEquals(2, actualSubjects.size());
        assertEquals("Mathematics", actualSubjects.get(0).getName());
        verify(subjectRepository, times(1)).findByTeacherId(TEACHER_ID);
    }

    @Test
    void getSubjectsByTeacher_shouldReturnEmptyList_whenNoSubjectsFound() {
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(subjectRepository.findByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

        List<Subject> actualSubjects = subjectService.getSubjectsByTeacher();

        assertNotNull(actualSubjects);
        assertTrue(actualSubjects.isEmpty());
        verify(subjectRepository, times(1)).findByTeacherId(TEACHER_ID);
    }

    @Test
    void createSubject_shouldSaveSubjectAndSetTeacherId_whenDataIsValid() {
        Subject subjectToCreate = Subject.builder().name("Valid Subject").build();

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> {
            Subject s = invocation.getArgument(0);
            return Subject.builder().id(1).name(s.getName()).teacherId(s.getTeacherId()).build();
        });

        Subject createdSubject = subjectService.createSubject(subjectToCreate);

        assertNotNull(createdSubject);
        assertEquals("Valid Subject", createdSubject.getName());
        assertEquals(TEACHER_ID, createdSubject.getTeacherId());
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    void createSubject_shouldThrowException_whenNameIsNull() {
        Subject subjectWithNullName = Subject.builder().name(null).build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Subject name is required.");

        SubjectValidationException exception = assertThrows(SubjectValidationException.class,
                () -> subjectService.createSubject(subjectWithNullName));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals("Subject name is required.", exception.getErrors().get(0).getMessage());
        verify(subjectRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
    }

    @Test
    void createSubject_shouldThrowException_whenNameIsEmpty() {
        Subject subjectWithEmptyName = Subject.builder().name("").build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Subject name is required.");

        SubjectValidationException exception = assertThrows(SubjectValidationException.class,
                () -> subjectService.createSubject(subjectWithEmptyName));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void createSubject_shouldThrowException_whenNameIsBlank() {
        Subject subjectWithBlankName = Subject.builder().name("   ").build();
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn("Subject name is required.");

        SubjectValidationException exception = assertThrows(SubjectValidationException.class,
                () -> subjectService.createSubject(subjectWithBlankName));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void getSubjectById_shouldReturnSubject_whenFound() {
        Integer subjectId = 1;
        Subject expectedSubject = Subject.builder().id(subjectId).name("Test Subject").build();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(expectedSubject));

        Optional<Subject> result = subjectService.getSubjectById(subjectId);

        assertTrue(result.isPresent());
        assertEquals(expectedSubject, result.get());
        verify(subjectRepository, times(1)).findById(subjectId);
    }

    @Test
    void getSubjectById_shouldReturnEmpty_whenNotFound() {
        Integer subjectId = 1;
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        Optional<Subject> result = subjectService.getSubjectById(subjectId);

        assertFalse(result.isPresent());
        verify(subjectRepository, times(1)).findById(subjectId);
    }

    @Test
    void softDeleteSubject_shouldCallRepository_whenSubjectExistsAndOwnedByTeacher() {
        Integer subjectId = 1;
        Subject subject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("Subject A").build();

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.softDeleteSubject(subjectId, TEACHER_ID)).thenReturn(subject);

        subjectService.softDeleteSubject(subjectId);

        verify(subjectRepository, times(1)).findById(subjectId);
        verify(subjectRepository, times(1)).softDeleteSubject(subjectId, TEACHER_ID);
    }

    @Test
    void softDeleteSubject_shouldThrowSubjectNotFoundException_whenSubjectDoesNotExist() {
        Integer subjectId = 1;
        String expectedErrorMessage = "Subject not found.";

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);

        SubjectNotFoundException exception = assertThrows(SubjectNotFoundException.class,
                () -> subjectService.softDeleteSubject(subjectId));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(subjectRepository, times(1)).findById(subjectId);
        verify(subjectRepository, never()).softDeleteSubject(any(), any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class));
    }

    @Test
    void softDeleteSubject_shouldThrowSubjectForbiddenException_whenSubjectNotOwnedByTeacher() {
        Integer subjectId = 1;
        Integer otherTeacherId = 999;
        Subject subject = Subject.builder().id(subjectId).teacherId(otherTeacherId).name("Subject A").build();
        String expectedErrorMessage = "You are not authorized to make changes to this subject.";

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);

        SubjectForbiddenException exception = assertThrows(SubjectForbiddenException.class,
                () -> subjectService.softDeleteSubject(subjectId));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(subjectRepository, times(1)).findById(subjectId);
        verify(subjectRepository, never()).softDeleteSubject(any(), any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class));
    }

    @Test
    void updateSubject_shouldUpdateSubject_whenDataIsValidAndOwnedByTeacher() {
        Integer subjectId = 1;
        Subject existingSubject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("Old Name").build();
        Subject updatedSubjectData = Subject.builder().name("New Name").build();
        Subject savedSubject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("New Name").build();

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(existingSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(savedSubject);

        Subject result = subjectService.updateSubject(subjectId, updatedSubjectData);

        assertNotNull(result);
        assertEquals(subjectId, result.getId());
        assertEquals("New Name", result.getName());
        verify(subjectRepository, times(1)).findById(subjectId);
        verify(subjectRepository, times(1)).save(existingSubject);
    }

    @Test
    void updateSubject_shouldThrowSubjectValidationException_whenNameIsNull() {
        Integer subjectId = 1;
        Subject updatedSubjectData = Subject.builder().name(null).build();
        String expectedErrorMessage = "Subject name is required.";

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                .thenReturn(expectedErrorMessage);

        SubjectValidationException exception = assertThrows(SubjectValidationException.class,
                () -> subjectService.updateSubject(subjectId, updatedSubjectData));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals(expectedErrorMessage, exception.getErrors().get(0).getMessage());
        verify(subjectRepository, never()).findById(any());
        verify(subjectRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
    }

    @Test
    void updateSubject_shouldThrowSubjectNotFoundException_whenSubjectDoesNotExist() {
        Integer subjectId = 1;
        Subject updatedSubjectData = Subject.builder().name("New Name").build();
        String expectedErrorMessage = "Subject not found.";

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);

        SubjectNotFoundException exception = assertThrows(SubjectNotFoundException.class,
                () -> subjectService.updateSubject(subjectId, updatedSubjectData));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(subjectRepository, times(1)).findById(subjectId);
        verify(subjectRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class));
    }

    @Test
    void updateSubject_shouldThrowSubjectForbiddenException_whenSubjectNotOwnedByTeacher() {
        Integer subjectId = 1;
        Integer otherTeacherId = 999;
        Subject existingSubject = Subject.builder().id(subjectId).teacherId(otherTeacherId).name("Old Name").build();
        Subject updatedSubjectData = Subject.builder().name("New Name").build();
        String expectedErrorMessage = "You are not authorized to make changes to this subject.";

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(existingSubject));
        when(messageSource.getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class)))
                .thenReturn(expectedErrorMessage);

        SubjectForbiddenException exception = assertThrows(SubjectForbiddenException.class,
                () -> subjectService.updateSubject(subjectId, updatedSubjectData));

        assertEquals(expectedErrorMessage, exception.getErrorDescription());
        verify(subjectRepository, times(1)).findById(subjectId);
        verify(subjectRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class));
    }

    @Test
    void updateSubject_shouldUseSpanishLocaleForValidationMessages_whenLocaleIsSpanish() {
        Integer subjectId = 1;
        Subject updatedSubjectData = Subject.builder().name(null).build();
        String expectedSpanishMessage = "El nombre de la asignatura es obligatorio.";
        Locale spanish = new Locale("es");

        when(sessionUser.getLocale()).thenReturn(spanish);
        when(messageSource.getMessage(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED, null, spanish))
                .thenReturn(expectedSpanishMessage);

        SubjectValidationException exception = assertThrows(SubjectValidationException.class,
                () -> subjectService.updateSubject(subjectId, updatedSubjectData));

        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        assertEquals(expectedSpanishMessage, exception.getErrors().get(0).getMessage());
        verify(subjectRepository, never()).findById(any());
        verify(subjectRepository, never()).save(any());
        verify(messageSource, times(1)).getMessage(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED, null, spanish);
    }
}
