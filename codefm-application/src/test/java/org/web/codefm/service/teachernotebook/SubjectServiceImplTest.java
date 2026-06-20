package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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

    private SubjectServiceImpl subjectService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        this.subjectService = new SubjectServiceImpl(this.subjectRepository, this.messageSource, this.sessionUser);
    }

    @Nested
    class GetSubjectsByTeacher {

        @Test
        void when_subjects_are_found_expect_subject_list() {
            final var expectedSubjects = Arrays.asList(
                    Subject.builder().id(1).name("Mathematics").teacherId(TEACHER_ID).build(),
                    Subject.builder().id(2).name("Science").teacherId(TEACHER_ID).build()
            );

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(subjectRepository.findByTeacherId(TEACHER_ID)).thenReturn(expectedSubjects);

            final var actualSubjects = subjectService.getSubjectsByTeacher();

            assertThat(actualSubjects).isNotNull().hasSize(2);
            assertThat(actualSubjects.get(0).getName()).isEqualTo("Mathematics");
            verify(subjectRepository, times(1)).findByTeacherId(TEACHER_ID);
        }

        @Test
        void when_no_subjects_are_found_expect_empty_list() {
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(subjectRepository.findByTeacherId(TEACHER_ID)).thenReturn(Collections.emptyList());

            final var actualSubjects = subjectService.getSubjectsByTeacher();

            assertThat(actualSubjects).isNotNull().isEmpty();
            verify(subjectRepository, times(1)).findByTeacherId(TEACHER_ID);
        }
    }

    @Nested
    class CreateSubject {

        @Test
        void when_data_is_valid_expect_subject_to_be_saved_and_teacher_id_to_be_set() {
            final Subject subjectToCreate = Subject.builder().name("Valid Subject").build();

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> {
                Subject s = invocation.getArgument(0);
                return Subject.builder().id(1).name(s.getName()).teacherId(s.getTeacherId()).build();
            });

            final Subject createdSubject = subjectService.createSubject(subjectToCreate);

            assertThat(createdSubject).isNotNull();
            assertThat(createdSubject.getName()).isEqualTo("Valid Subject");
            assertThat(createdSubject.getTeacherId()).isEqualTo(TEACHER_ID);
            verify(subjectRepository, times(1)).save(any(Subject.class));
        }

        @Test
        void when_name_is_null_expect_validation_exception() {
            final Subject subjectWithNullName = Subject.builder().name(null).build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn("Subject name is required.");

            final ThrowingCallable call = () -> subjectService.createSubject(subjectWithNullName);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectValidationException.class);
            final SubjectValidationException exception = (SubjectValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Subject name is required.");
            verify(subjectRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
        }

        @Test
        void when_name_is_empty_expect_validation_exception() {
            final Subject subjectWithEmptyName = Subject.builder().name("").build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn("Subject name is required.");

            final ThrowingCallable call = () -> subjectService.createSubject(subjectWithEmptyName);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectValidationException.class);
            final SubjectValidationException exception = (SubjectValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            verify(subjectRepository, never()).save(any());
        }

        @Test
        void when_name_is_blank_expect_validation_exception() {
            final Subject subjectWithBlankName = Subject.builder().name("   ").build();
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn("Subject name is required.");

            final ThrowingCallable call = () -> subjectService.createSubject(subjectWithBlankName);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectValidationException.class);
            final SubjectValidationException exception = (SubjectValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            verify(subjectRepository, never()).save(any());
        }
    }

    @Nested
    class GetSubjectById {

        @Test
        void when_subject_is_found_expect_optional_with_value() {
            final Integer subjectId = 1;
            final Subject expectedSubject = Subject.builder().id(subjectId).name("Test Subject").build();
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(expectedSubject));

            final Optional<Subject> result = subjectService.getSubjectById(subjectId);

            assertThat(result).isPresent().contains(expectedSubject);
            verify(subjectRepository, times(1)).findById(subjectId);
        }

        @Test
        void when_subject_is_not_found_expect_empty_optional() {
            final Integer subjectId = 1;
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

            final Optional<Subject> result = subjectService.getSubjectById(subjectId);

            assertThat(result).isEmpty();
            verify(subjectRepository, times(1)).findById(subjectId);
        }
    }

    @Nested
    class SoftDeleteSubject {

        @Test
        void when_subject_exists_and_is_owned_by_teacher_expect_repository_call() {
            final Integer subjectId = 1;
            final Subject subject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("Subject A").build();

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
            when(subjectRepository.softDeleteSubject(subjectId, TEACHER_ID)).thenReturn(subject);

            subjectService.softDeleteSubject(subjectId);

            verify(subjectRepository, times(1)).findById(subjectId);
            verify(subjectRepository, times(1)).softDeleteSubject(subjectId, TEACHER_ID);
        }

        @Test
        void when_subject_does_not_exist_expect_subject_not_found_exception() {
            final Integer subjectId = 1;
            final String expectedErrorMessage = "Subject not found.";

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);

            final ThrowingCallable call = () -> subjectService.softDeleteSubject(subjectId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectNotFoundException.class);
            final SubjectNotFoundException exception = (SubjectNotFoundException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(subjectRepository, times(1)).findById(subjectId);
            verify(subjectRepository, never()).softDeleteSubject(any(), any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class));
        }

        @Test
        void when_subject_is_not_owned_by_teacher_expect_subject_forbidden_exception() {
            final Integer subjectId = 1;
            final Integer otherTeacherId = 999;
            final Subject subject = Subject.builder().id(subjectId).teacherId(otherTeacherId).name("Subject A").build();
            final String expectedErrorMessage = "You are not authorized to make changes to this subject.";

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);

            final ThrowingCallable call = () -> subjectService.softDeleteSubject(subjectId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectForbiddenException.class);
            final SubjectForbiddenException exception = (SubjectForbiddenException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(subjectRepository, times(1)).findById(subjectId);
            verify(subjectRepository, never()).softDeleteSubject(any(), any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class));
        }
    }

    @Nested
    class UpdateSubject {

        @Test
        void when_data_is_valid_and_subject_is_owned_by_teacher_expect_subject_to_be_updated() {
            final Integer subjectId = 1;
            final Subject existingSubject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("Old Name").build();
            final Subject updatedSubjectData = Subject.builder().name("New Name").build();
            final Subject savedSubject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("New Name").build();

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(existingSubject));
            when(subjectRepository.save(any(Subject.class))).thenReturn(savedSubject);

            final Subject result = subjectService.updateSubject(subjectId, updatedSubjectData);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subjectId);
            assertThat(result.getName()).isEqualTo("New Name");
            verify(subjectRepository, times(1)).findById(subjectId);
            verify(subjectRepository, times(1)).save(existingSubject);
        }

        @Test
        void when_name_is_null_expect_subject_validation_exception() {
            final Integer subjectId = 1;
            final Subject updatedSubjectData = Subject.builder().name(null).build();
            final String expectedErrorMessage = "Subject name is required.";

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);

            final ThrowingCallable call = () -> subjectService.updateSubject(subjectId, updatedSubjectData);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectValidationException.class);
            final SubjectValidationException exception = (SubjectValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo(expectedErrorMessage);
            verify(subjectRepository, never()).findById(any());
            verify(subjectRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
        }

        @Test
        void when_subject_does_not_exist_expect_subject_not_found_exception() {
            final Integer subjectId = 1;
            final Subject updatedSubjectData = Subject.builder().name("New Name").build();
            final String expectedErrorMessage = "Subject not found.";

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);

            final ThrowingCallable call = () -> subjectService.updateSubject(subjectId, updatedSubjectData);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectNotFoundException.class);
            final SubjectNotFoundException exception = (SubjectNotFoundException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(subjectRepository, times(1)).findById(subjectId);
            verify(subjectRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_NOT_FOUND), any(), any(Locale.class));
        }

        @Test
        void when_subject_is_not_owned_by_teacher_expect_subject_forbidden_exception() {
            final Integer subjectId = 1;
            final Integer otherTeacherId = 999;
            final Subject existingSubject = Subject.builder().id(subjectId).teacherId(otherTeacherId).name("Old Name").build();
            final Subject updatedSubjectData = Subject.builder().name("New Name").build();
            final String expectedErrorMessage = "You are not authorized to make changes to this subject.";

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(existingSubject));
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);

            final ThrowingCallable call = () -> subjectService.updateSubject(subjectId, updatedSubjectData);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectForbiddenException.class);
            final SubjectForbiddenException exception = (SubjectForbiddenException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(subjectRepository, times(1)).findById(subjectId);
            verify(subjectRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SUBJECT_FORBIDDEN), any(), any(Locale.class));
        }

        @Test
        void when_locale_is_spanish_expect_translated_validation_message() {
            final Integer subjectId = 1;
            final Subject updatedSubjectData = Subject.builder().name(null).build();
            final String expectedSpanishMessage = "El nombre de la asignatura es obligatorio.";
            final Locale spanish = new Locale("es");

            when(sessionUser.getLocale()).thenReturn(spanish);
            when(messageSource.getMessage(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED, null, spanish))
                    .thenReturn(expectedSpanishMessage);

            final ThrowingCallable call = () -> subjectService.updateSubject(subjectId, updatedSubjectData);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SubjectValidationException.class);
            final SubjectValidationException exception = (SubjectValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo(expectedSpanishMessage);
            verify(subjectRepository, never()).findById(any());
            verify(subjectRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(MessageKeys.SUBJECT_VALIDATION_NAME_REQUIRED, null, spanish);
        }
    }
}
