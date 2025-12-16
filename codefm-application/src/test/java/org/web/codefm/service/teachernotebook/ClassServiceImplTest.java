package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.ClassValidationException;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassServiceImplTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SchoolService schoolService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private ClassServiceImpl classService;

    @Test
    void getActiveClassesBySchoolIdAndTeacherId_shouldReturnClasses_whenSchoolExistsAndTeacherOwnsIt() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        Class class1 = Class.builder().id(1).schoolId(schoolId).name("Math Class").schoolYear("24/25").build();
        List<Class> expectedClasses = Arrays.asList(class1);

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(expectedClasses);

        // When
        List<Class> result = classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
    }

    @Test
    void getActiveClassesBySchoolIdAndTeacherId_shouldThrowSchoolNotFoundException_whenSchoolDoesNotExist() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, Locale.ENGLISH)).thenReturn("School not found");

        // When & Then
        assertThrows(SchoolNotFoundException.class,
                () -> classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId));

        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, never()).findActiveClassesBySchoolIdAndTeacherId(anyInt(), anyInt());
    }

    @Test
    void getActiveClassesBySchoolIdAndTeacherId_shouldThrowSchoolForbiddenException_whenTeacherDoesNotOwnSchool() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        Integer differentTeacherId = 2;
        School school = School.builder().id(schoolId).teacherId(differentTeacherId).name("School A").build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, Locale.ENGLISH)).thenReturn("Forbidden");

        // When & Then
        assertThrows(SchoolForbiddenException.class,
                () -> classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId));

        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, never()).findActiveClassesBySchoolIdAndTeacherId(anyInt(), anyInt());
    }

    @Test
    void createClass_shouldCreateClass_whenDataIsValid() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        Class createdClass = Class.builder()
                .id(1)
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(classRepository.save(classToCreate)).thenReturn(createdClass);

        // When
        Class result = classService.createClass(classToCreate, teacherId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Math Class", result.getName());
        assertEquals("24/25", result.getSchoolYear());
        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, times(1)).save(classToCreate);
    }

    @Test
    void createClass_shouldThrowException_whenNameIsEmpty() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("")
                .schoolYear("24/25")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH))
                .thenReturn("Class name is required.");

        // When & Then
        ClassValidationException exception = assertThrows(ClassValidationException.class,
                () -> classService.createClass(classToCreate, teacherId));

        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());
        assertEquals("name", exception.getErrors().get(0).getParam());
        verify(classRepository, never()).save(any());
    }

    @Test
    void createClass_shouldThrowException_whenSchoolYearIsEmpty() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_REQUIRED, null, Locale.ENGLISH))
                .thenReturn("School year is required.");

        // When & Then
        ClassValidationException exception = assertThrows(ClassValidationException.class,
                () -> classService.createClass(classToCreate, teacherId));

        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());
        assertEquals("schoolYear", exception.getErrors().get(0).getParam());
        verify(classRepository, never()).save(any());
    }

    @Test
    void createClass_shouldThrowException_whenSchoolYearFormatIsInvalid() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("2024/2025")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_FORMAT_INVALID, null, Locale.ENGLISH))
                .thenReturn("School year must be in format NN/NN (e.g., 24/25).");

        // When & Then
        ClassValidationException exception = assertThrows(ClassValidationException.class,
                () -> classService.createClass(classToCreate, teacherId));

        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());
        assertEquals("schoolYear", exception.getErrors().get(0).getParam());
        verify(classRepository, never()).save(any());
    }

    @Test
    void createClass_shouldThrowException_whenSchoolYearIsNotConsecutive() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/26")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_NOT_CONSECUTIVE, null, Locale.ENGLISH))
                .thenReturn("School year numbers must be consecutive (e.g., 24/25).");

        // When & Then
        ClassValidationException exception = assertThrows(ClassValidationException.class,
                () -> classService.createClass(classToCreate, teacherId));

        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());
        assertEquals("schoolYear", exception.getErrors().get(0).getParam());
        verify(classRepository, never()).save(any());
    }

    @Test
    void createClass_shouldThrowException_whenSchoolNotFound() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, Locale.ENGLISH))
                .thenReturn("School not found");

        // When & Then
        assertThrows(SchoolNotFoundException.class,
                () -> classService.createClass(classToCreate, teacherId));

        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, never()).save(any());
    }

    @Test
    void createClass_shouldThrowException_whenSchoolNotOwnedByTeacher() {
        // Given
        Integer schoolId = 1;
        Integer teacherId = 1;
        Integer differentTeacherId = 2;
        School school = School.builder().id(schoolId).teacherId(differentTeacherId).name("School A").build();

        Class classToCreate = Class.builder()
                .schoolId(schoolId)
                .name("Math Class")
                .schoolYear("24/25")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, Locale.ENGLISH))
                .thenReturn("Forbidden");

        // When & Then
        assertThrows(SchoolForbiddenException.class,
                () -> classService.createClass(classToCreate, teacherId));

        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, never()).save(any());
    }
}

