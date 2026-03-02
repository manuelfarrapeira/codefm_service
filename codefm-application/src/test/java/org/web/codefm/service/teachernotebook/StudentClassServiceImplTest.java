package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassValidationException;
import org.web.codefm.domain.exception.teachernotebook.StudentNotFoundException;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentClassServiceImplTest {

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private StudentClassServiceImpl studentClassService;

    private final Integer teacherId = 1;
    private final Integer classId = 10;
    private final Integer studentId = 20;

    @BeforeEach
    void setUp() {
        Map<String, String> sessionParameters = new HashMap<>();
        sessionParameters.put(SessionParameter.TEACHER_ID.getClaimName(), teacherId.toString());
        lenient().when(sessionUser.getParameters()).thenReturn(sessionParameters);
    }

    @Test
    void addStudentToClass_shouldCreateNewAssociation_whenNotExists() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());

        studentClassService.addStudentToClass(classId, studentId);

        verify(classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId);
        verify(studentRepository).findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId);
        verify(studentClassRepository).findByClassIdAndStudentId(classId, studentId);
        verify(studentClassRepository).save(any(StudentClass.class));
        verify(studentClassRepository, never()).update(any(StudentClass.class));
    }

    @Test
    void addStudentToClass_shouldReactivateAssociation_whenExistsAndDeleted() {
        StudentClass deletedAssociation = StudentClass.builder()
                .id(1).classId(classId).studentId(studentId)
                .deletionDate(LocalDate.now().minusDays(5)).build();

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(deletedAssociation));

        studentClassService.addStudentToClass(classId, studentId);

        verify(studentClassRepository).update(any(StudentClass.class));
        verify(studentClassRepository, never()).save(any(StudentClass.class));
        assertNull(deletedAssociation.getDeletionDate());
    }

    @Test
    void addStudentToClass_shouldThrowValidationException_whenAssociationAlreadyActive() {
        StudentClass activeAssociation = StudentClass.builder()
                .id(1).classId(classId).studentId(studentId).deletionDate(null).build();

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(activeAssociation));
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentClassValidationException.class, () ->
                studentClassService.addStudentToClass(classId, studentId));

        verify(studentClassRepository, never()).save(any(StudentClass.class));
        verify(studentClassRepository, never()).update(any(StudentClass.class));
    }

    @Test
    void addStudentToClass_shouldThrowClassNotFoundException_whenClassNotFound() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.empty());
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(ClassNotFoundException.class, () ->
                studentClassService.addStudentToClass(classId, studentId));

        verify(studentRepository, never()).findByIdAndTeacherIdAndDeletionDateIsNull(anyInt(), anyInt());
        verify(studentClassRepository, never()).findByClassIdAndStudentId(anyInt(), anyInt());
    }

    @Test
    void addStudentToClass_shouldThrowStudentNotFoundException_whenStudentNotFound() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentNotFoundException.class, () ->
                studentClassService.addStudentToClass(classId, studentId));

        verify(studentClassRepository, never()).findByClassIdAndStudentId(anyInt(), anyInt());
    }

    @Test
    void findActiveAssociation_shouldReturnAssociation_whenActiveAndOwned() {
        StudentClass activeAssociation = StudentClass.builder()
                .id(1).classId(classId).studentId(studentId).deletionDate(null).build();

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(activeAssociation));

        StudentClass result = studentClassService.findActiveAssociation(classId, studentId);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(studentClassRepository).findByClassIdAndStudentId(classId, studentId);
    }

    @Test
    void findActiveAssociation_shouldThrowNotFoundException_whenAssociationDoesNotExist() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentClassNotFoundException.class, () ->
                studentClassService.findActiveAssociation(classId, studentId));
    }

    @Test
    void findActiveAssociation_shouldThrowNotFoundException_whenAssociationIsDeleted() {
        StudentClass deletedAssociation = StudentClass.builder()
                .id(1).classId(classId).studentId(studentId)
                .deletionDate(LocalDate.now().minusDays(1)).build();

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(deletedAssociation));
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentClassNotFoundException.class, () ->
                studentClassService.findActiveAssociation(classId, studentId));
    }

    @Test
    void removeStudentFromClass_shouldSoftDeleteAssociation() {
        doNothing().when(studentClassRepository).softDelete(classId, studentId);

        studentClassService.removeStudentFromClass(classId, studentId);

        verify(studentClassRepository).softDelete(classId, studentId);
    }

    @Test
    void removeStudentFromClass_shouldThrowNotFoundException_whenAssociationNotFound() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.empty());
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentClassNotFoundException.class, () ->
                studentClassService.findActiveAssociation(classId, studentId));

        verify(studentClassRepository, never()).softDelete(anyInt(), anyInt());
    }

    @Test
    void removeStudentFromClass_shouldThrowNotFoundException_whenAssociationAlreadyDeleted() {
        StudentClass deletedAssociation = StudentClass.builder()
                .id(1).classId(classId).studentId(studentId)
                .deletionDate(LocalDate.now().minusDays(5)).build();

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.of(Student.builder().id(studentId).build()));
        when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                .thenReturn(Optional.of(deletedAssociation));
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentClassNotFoundException.class, () ->
                studentClassService.findActiveAssociation(classId, studentId));

        verify(studentClassRepository, never()).softDelete(anyInt(), anyInt());
    }

    @Test
    void removeStudentFromClass_shouldThrowClassNotFoundException_whenClassNotFound() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.empty());
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(ClassNotFoundException.class, () ->
                studentClassService.findActiveAssociation(classId, studentId));

        verify(studentRepository, never()).findByIdAndTeacherIdAndDeletionDateIsNull(anyInt(), anyInt());
        verify(studentClassRepository, never()).findByClassIdAndStudentId(anyInt(), anyInt());
    }

    @Test
    void removeStudentFromClass_shouldThrowStudentNotFoundException_whenStudentNotFound() {
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                .thenReturn(Optional.of(Class.builder().id(classId).build()));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                .thenReturn(Optional.empty());
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

        assertThrows(StudentNotFoundException.class, () ->
                studentClassService.findActiveAssociation(classId, studentId));

        verify(studentClassRepository, never()).findByClassIdAndStudentId(anyInt(), anyInt());
    }
}

