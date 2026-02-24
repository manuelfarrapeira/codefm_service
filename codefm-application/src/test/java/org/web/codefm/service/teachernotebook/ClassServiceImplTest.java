package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.*;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassServiceImplTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SchoolService schoolService;

    @Mock
    private SubjectClassRepository subjectClassRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseDocumentService exerciseDocumentService;

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

    @Test
    void softDeleteClass_shouldCallRepository_whenClassExistsAndSchoolOwnedByTeacher() {
        Integer classId = 1;
        Integer teacherId = 1;
        Integer schoolId = 10;

        Class clazz = Class.builder()
                .id(classId)
                .schoolId(schoolId)
                .name("Test Class")
                .schoolYear("24/25")
                .build();

        School school = School.builder()
                .id(schoolId)
                .teacherId(teacherId)
                .name("Test School")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(classId)).thenReturn(Optional.of(clazz));
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(subjectClassRepository.findActiveIdsByClassId(classId)).thenReturn(Collections.emptyList());
        when(classRepository.softDeleteClass(classId, teacherId)).thenReturn(clazz);

        classService.softDeleteClass(classId, teacherId);

        verify(classRepository, times(1)).findById(classId);
        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(studentClassRepository, times(1)).softDeleteByClassId(classId);
        verify(subjectClassRepository, times(1)).softDeleteByClassId(classId);
        verify(scheduleRepository, times(1)).softDeleteByClassId(classId);
        verify(classRepository, times(1)).softDeleteClass(classId, teacherId);
    }

    @Test
    void softDeleteClass_shouldCascadeDeleteAllDependencies() {
        Integer classId = 1;
        Integer teacherId = 1;
        Integer schoolId = 10;

        Class clazz = Class.builder().id(classId).schoolId(schoolId).name("Test Class").schoolYear("24/25").build();
        School school = School.builder().id(schoolId).teacherId(teacherId).name("Test School").build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(classId)).thenReturn(Optional.of(clazz));
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(subjectClassRepository.findActiveIdsByClassId(classId)).thenReturn(Arrays.asList(100, 101));
        when(classRepository.softDeleteClass(classId, teacherId)).thenReturn(clazz);

        classService.softDeleteClass(classId, teacherId);

        verify(exerciseRepository, times(1)).softDeleteBySubjectClassIds(Arrays.asList(100, 101));
        verify(studentClassRepository, times(1)).softDeleteByClassId(classId);
        verify(subjectClassRepository, times(1)).softDeleteByClassId(classId);
        verify(scheduleRepository, times(1)).softDeleteByClassId(classId);
        verify(classRepository, times(1)).softDeleteClass(classId, teacherId);
    }

    @Test
    void softDeleteClass_shouldThrowClassNotFoundException_whenClassDoesNotExist() {
        // Given
        Integer classId = 999;
        Integer teacherId = 1;

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(classId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, Locale.ENGLISH))
                .thenReturn("Class not found.");

        // When & Then
        assertThrows(ClassNotFoundException.class,
                () -> classService.softDeleteClass(classId, teacherId));

        verify(classRepository, times(1)).findById(classId);
        verify(classRepository, never()).softDeleteClass(any(), any());
    }

    @Test
    void softDeleteClass_shouldThrowClassForbiddenException_whenSchoolNotOwnedByTeacher() {
        // Given
        Integer classId = 1;
        Integer teacherId = 1;
        Integer schoolId = 10;
        Integer differentTeacherId = 999;

        Class clazz = Class.builder()
                .id(classId)
                .schoolId(schoolId)
                .name("Test Class")
                .schoolYear("24/25")
                .build();

        School school = School.builder()
                .id(schoolId)
                .teacherId(differentTeacherId)
                .name("Test School")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(classId)).thenReturn(Optional.of(clazz));
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
        when(messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, Locale.ENGLISH))
                .thenReturn("You are not authorized to delete this class.");

        // When & Then
        assertThrows(ClassForbiddenException.class,
                () -> classService.softDeleteClass(classId, teacherId));

        verify(classRepository, times(1)).findById(classId);
        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, never()).softDeleteClass(any(), any());
    }

    @Test
    void softDeleteClass_shouldThrowClassForbiddenException_whenSchoolNotFound() {
        // Given
        Integer classId = 1;
        Integer teacherId = 1;
        Integer schoolId = 10;

        Class clazz = Class.builder()
                .id(classId)
                .schoolId(schoolId)
                .name("Test Class")
                .schoolYear("24/25")
                .build();

        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(classRepository.findById(classId)).thenReturn(Optional.of(clazz));
        when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, Locale.ENGLISH))
                .thenReturn("You are not authorized to delete this class.");

        // When & Then
        assertThrows(ClassForbiddenException.class,
                () -> classService.softDeleteClass(classId, teacherId));

        verify(classRepository, times(1)).findById(classId);
        verify(schoolService, times(1)).getSchoolById(schoolId);
        verify(classRepository, never()).softDeleteClass(any(), any());
    }

  @Test
  void updateClass_shouldUpdateClass_whenDataIsValidAndTeacherOwnsSchool() {
    // Given
    Integer classId = 1;
    Integer teacherId = 1;
    Integer schoolId = 10;

    Class existingClass = Class.builder()
        .id(classId)
        .schoolId(schoolId)
        .name("Old Name")
        .schoolYear("23/24")
        .build();

    Class updateData = Class.builder()
        .name("New Name")
        .schoolYear("24/25")
        .build();

    Class updatedClass = Class.builder()
        .id(classId)
        .schoolId(schoolId)
        .name("New Name")
        .schoolYear("24/25")
        .build();

    School school = School.builder()
        .id(schoolId)
        .teacherId(teacherId)
        .name("Test School")
        .build();

    when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    when(classRepository.findById(classId)).thenReturn(Optional.of(existingClass));
    when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
    when(classRepository.save(any(Class.class))).thenReturn(updatedClass);

    // When
    Class result = classService.updateClass(classId, updateData, teacherId);

    // Then
    assertNotNull(result);
    assertEquals("New Name", result.getName());
    assertEquals("24/25", result.getSchoolYear());
    verify(classRepository, times(1)).findById(classId);
    verify(schoolService, times(1)).getSchoolById(schoolId);
    verify(classRepository, times(1)).save(any(Class.class));
  }

  @Test
  void updateClass_shouldThrowClassNotFoundException_whenClassDoesNotExist() {
    // Given
    Integer classId = 999;
    Integer teacherId = 1;
    Class updateData = Class.builder().name("New Name").schoolYear("24/25").build();

    when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    when(classRepository.findById(classId)).thenReturn(Optional.empty());
    when(messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, Locale.ENGLISH))
        .thenReturn("Class not found.");

    // When & Then
    assertThrows(ClassNotFoundException.class,
        () -> classService.updateClass(classId, updateData, teacherId));

    verify(classRepository, times(1)).findById(classId);
    verify(classRepository, never()).save(any());
  }

  @Test
  void updateClass_shouldThrowClassValidationException_whenNameIsEmpty() {
    // Given
    Integer classId = 1;
    Integer teacherId = 1;
    Class updateData = Class.builder().name("").schoolYear("24/25").build();

    when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH))
        .thenReturn("Class name is required.");

    // When & Then
    ClassValidationException exception = assertThrows(ClassValidationException.class,
        () -> classService.updateClass(classId, updateData, teacherId));

    assertNotNull(exception.getErrors());
    assertEquals(1, exception.getErrors().size());
    verify(classRepository, never()).findById(any());
    verify(classRepository, never()).save(any());
  }

  @Test
  void updateClass_shouldThrowClassValidationException_whenSchoolYearIsInvalid() {
    // Given
    Integer classId = 1;
    Integer teacherId = 1;
    Class updateData = Class.builder().name("Valid Name").schoolYear("23/25").build();

    when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_NOT_CONSECUTIVE, null, Locale.ENGLISH))
        .thenReturn("School year numbers must be consecutive.");

    // When & Then
    ClassValidationException exception = assertThrows(ClassValidationException.class,
        () -> classService.updateClass(classId, updateData, teacherId));

    assertNotNull(exception.getErrors());
    verify(classRepository, never()).findById(any());
    verify(classRepository, never()).save(any());
  }

  @Test
  void updateClass_shouldThrowClassForbiddenException_whenTeacherDoesNotOwnSchool() {
    // Given
    Integer classId = 1;
    Integer teacherId = 1;
    Integer schoolId = 10;
    Integer differentTeacherId = 999;

    Class existingClass = Class.builder()
        .id(classId)
        .schoolId(schoolId)
        .name("Old Name")
        .schoolYear("23/24")
        .build();

    Class updateData = Class.builder()
        .name("New Name")
        .schoolYear("24/25")
        .build();

    School school = School.builder()
        .id(schoolId)
        .teacherId(differentTeacherId)
        .name("Test School")
        .build();

    when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    when(classRepository.findById(classId)).thenReturn(Optional.of(existingClass));
    when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
    when(messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, Locale.ENGLISH))
        .thenReturn("You are not authorized to update this class.");

    // When & Then
    assertThrows(ClassForbiddenException.class,
        () -> classService.updateClass(classId, updateData, teacherId));

    verify(classRepository, times(1)).findById(classId);
    verify(schoolService, times(1)).getSchoolById(schoolId);
    verify(classRepository, never()).save(any());
  }
}

