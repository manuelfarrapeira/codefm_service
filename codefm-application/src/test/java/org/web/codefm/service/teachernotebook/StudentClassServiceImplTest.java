package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private StudentClassServiceImpl studentClassService;

    private final Integer teacherId = 1;
    private final Integer classId = 10;
    private final Integer studentId = 20;

    @BeforeEach
    void beforeEach() {
        studentClassService = new StudentClassServiceImpl(
                studentClassRepository, classRepository, studentRepository, messageSource, sessionUser);
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(teacherId);
    }

    @Nested
    class AddStudentToClass {

        @Test
        void when_association_not_exists_expect_create_new_association() {
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
        void when_association_exists_and_deleted_expect_reactivate() {
            final StudentClass deletedAssociation = StudentClass.builder()
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
            assertThat(deletedAssociation.getDeletionDate()).isNull();
        }

        @Test
        void when_association_already_active_expect_throw_validation_exception() {
            final StudentClass activeAssociation = StudentClass.builder()
                    .id(1).classId(classId).studentId(studentId).deletionDate(null).build();

            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.of(Class.builder().id(classId).build()));
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(Student.builder().id(studentId).build()));
            when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                    .thenReturn(Optional.of(activeAssociation));
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable action = () -> studentClassService.addStudentToClass(classId, studentId);
            assertThatThrownBy(action).isInstanceOf(StudentClassValidationException.class);

            verify(studentClassRepository, never()).save(any(StudentClass.class));
            verify(studentClassRepository, never()).update(any(StudentClass.class));
        }

        @Test
        void when_class_not_found_expect_throw_class_not_found_exception() {
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.empty());
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable action = () -> studentClassService.addStudentToClass(classId, studentId);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);

            verify(studentRepository, never()).findByIdAndTeacherIdAndDeletionDateIsNull(anyInt(), anyInt());
            verify(studentClassRepository, never()).findByClassIdAndStudentId(anyInt(), anyInt());
        }

        @Test
        void when_student_not_found_expect_throw_student_not_found_exception() {
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.of(Class.builder().id(classId).build()));
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.empty());
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable action = () -> studentClassService.addStudentToClass(classId, studentId);
            assertThatThrownBy(action).isInstanceOf(StudentNotFoundException.class);

            verify(studentClassRepository, never()).findByClassIdAndStudentId(anyInt(), anyInt());
        }
    }

    @Nested
    class FindActiveAssociation {

        @Test
        void when_association_active_and_owned_expect_return_association() {
            final StudentClass activeAssociation = StudentClass.builder()
                    .id(1).classId(classId).studentId(studentId).deletionDate(null).build();

            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.of(Class.builder().id(classId).build()));
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(Student.builder().id(studentId).build()));
            when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                    .thenReturn(Optional.of(activeAssociation));

            final StudentClass result = studentClassService.findActiveAssociation(classId, studentId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(studentClassRepository).findByClassIdAndStudentId(classId, studentId);
        }

        @Test
        void when_association_does_not_exist_expect_throw_not_found_exception() {
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId))
                    .thenReturn(Optional.of(Class.builder().id(classId).build()));
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId))
                    .thenReturn(Optional.of(Student.builder().id(studentId).build()));
            when(studentClassRepository.findByClassIdAndStudentId(classId, studentId))
                    .thenReturn(Optional.empty());
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Error message");

            final ThrowingCallable action = () -> studentClassService.findActiveAssociation(classId, studentId);
            assertThatThrownBy(action).isInstanceOf(StudentClassNotFoundException.class);
        }

        @Test
        void when_association_is_deleted_expect_throw_not_found_exception() {
            final StudentClass deletedAssociation = StudentClass.builder()
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

            final ThrowingCallable action = () -> studentClassService.findActiveAssociation(classId, studentId);
            assertThatThrownBy(action).isInstanceOf(StudentClassNotFoundException.class);
        }
    }

    @Nested
    class RemoveStudentFromClass {

        @Test
        void when_valid_expect_soft_delete_association() {
            doNothing().when(studentClassRepository).softDelete(classId, studentId);

            studentClassService.removeStudentFromClass(classId, studentId);

            verify(studentClassRepository).softDelete(classId, studentId);
        }

    }
}

