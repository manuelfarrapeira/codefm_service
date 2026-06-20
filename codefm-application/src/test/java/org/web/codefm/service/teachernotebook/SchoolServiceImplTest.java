package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.SchoolForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.SchoolNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SchoolValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {

    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    private SchoolServiceImpl schoolService;

    @BeforeEach
    void beforeEach() {
        this.schoolService = new SchoolServiceImpl(this.schoolRepository, this.messageSource, this.sessionUser);
    }

    @Nested
    class GetSchoolsByTeacherId {

        @Test
        void when_schools_are_found_expect_school_list() {
            final Integer teacherId = 1;
            final List<School> expectedSchools = Arrays.asList(
                    School.builder().id(1).name("School A").build(),
                    School.builder().id(2).name("School B").build()
            );

            when(schoolRepository.findByTeacherId(teacherId)).thenReturn(expectedSchools);

            final List<School> actualSchools = schoolService.getSchoolsByTeacherId(teacherId);

            assertThat(actualSchools).isNotNull().hasSize(2);
            assertThat(actualSchools.get(0).getName()).isEqualTo("School A");
            verify(schoolRepository, times(1)).findByTeacherId(teacherId);
        }

        @Test
        void when_no_schools_are_found_expect_empty_list() {
            final Integer teacherId = 2;
            when(schoolRepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());

            final List<School> actualSchools = schoolService.getSchoolsByTeacherId(teacherId);

            assertThat(actualSchools).isNotNull().isEmpty();
            verify(schoolRepository, times(1)).findByTeacherId(teacherId);
        }
    }

    @Nested
    class CreateSchool {

        @Test
        void when_data_is_valid_expect_school_to_be_saved() {
            final School schoolToCreate = School.builder()
                    .name("Valid School")
                    .tlf(123456789)
                    .build();

            when(schoolRepository.save(schoolToCreate)).thenReturn(schoolToCreate);
            final School createdSchool = schoolService.createSchool(schoolToCreate);

            assertThat(createdSchool).isNotNull();
            assertThat(createdSchool.getName()).isEqualTo("Valid School");
            verify(schoolRepository, times(1)).save(schoolToCreate);
        }

        @Test
        void when_tlf_is_invalid_expect_validation_exception() {
            final School schoolWithInvalidTlf = School.builder()
                    .name("Valid School")
                    .tlf(123)
                    .build();
            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                    .thenReturn("Telephone number must be 9 digits.");
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.createSchool(schoolWithInvalidTlf);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolValidationException.class);
            final SchoolValidationException exception = (SchoolValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("tlf");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo("Telephone number must be 9 digits.");
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class));
        }

        @Test
        void when_multiple_fields_are_invalid_expect_validation_exception() {
            final School schoolWithMultipleErrors = School.builder()
                    .name("")
                    .tlf(12345)
                    .build();

            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn("School name is required.");
            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                    .thenReturn("Telephone number must be 9 digits.");
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.createSchool(schoolWithMultipleErrors);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolValidationException.class);
            final SchoolValidationException exception = (SchoolValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(2);
            assertThat(exception.getErrors()).anyMatch(e ->
                    "name".equals(e.getParam()) && "School name is required.".equals(e.getMessage()));
            assertThat(exception.getErrors()).anyMatch(e ->
                    "tlf".equals(e.getParam()) && "Telephone number must be 9 digits.".equals(e.getMessage()));
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class));
        }
    }

    @Nested
    class SoftDeleteSchool {

        @Test
        void when_school_exists_and_is_owned_by_teacher_expect_repository_call() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
            when(schoolRepository.softDeleteSchool(schoolId, teacherId)).thenReturn(school);

            schoolService.softDeleteSchool(schoolId, teacherId);

            verify(schoolRepository, times(1)).findById(schoolId);
            verify(schoolRepository, times(1)).softDeleteSchool(schoolId, teacherId);
        }

        @Test
        void when_school_does_not_exist_expect_school_not_found_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final String expectedErrorMessage = "School with ID 1 not found.";

            when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.softDeleteSchool(schoolId, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolNotFoundException.class);
            final SchoolNotFoundException exception = (SchoolNotFoundException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(schoolRepository, times(1)).findById(schoolId);
            verify(schoolRepository, never()).softDeleteSchool(any(), any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class));
        }

        @Test
        void when_school_is_not_owned_by_teacher_expect_school_forbidden_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final Integer otherTeacherId = 999;
            final School school = School.builder().id(schoolId).teacherId(otherTeacherId).name("School A").build();
            final String expectedErrorMessage = "You are not authorized to delete this school.";

            when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.softDeleteSchool(schoolId, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolForbiddenException.class);
            final SchoolForbiddenException exception = (SchoolForbiddenException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(schoolRepository, times(1)).findById(schoolId);
            verify(schoolRepository, never()).softDeleteSchool(any(), any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class));
        }
    }

    @Nested
    class GetSchoolById {

        @Test
        void when_school_is_found_expect_optional_with_value() {
            final Integer schoolId = 1;
            final School expectedSchool = School.builder().id(schoolId).name("Test School").build();
            when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(expectedSchool));

            final Optional<School> result = schoolService.getSchoolById(schoolId);

            assertThat(result).isPresent().contains(expectedSchool);
            verify(schoolRepository, times(1)).findById(schoolId);
        }

        @Test
        void when_school_is_not_found_expect_empty_optional() {
            final Integer schoolId = 1;
            when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());

            final Optional<School> result = schoolService.getSchoolById(schoolId);

            assertThat(result).isEmpty();
            verify(schoolRepository, times(1)).findById(schoolId);
        }
    }

    @Nested
    class UpdateSchool {

        @Test
        void when_data_is_valid_and_school_is_owned_by_teacher_expect_school_to_be_updated() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final School existingSchool = School.builder().id(schoolId).teacherId(teacherId).name("Old Name").town("Old Town").tlf(111222333).build();
            final School updatedSchoolData = School.builder().name("New Name").town("New Town").tlf(987654321).build();
            final School savedSchool = School.builder().id(schoolId).teacherId(teacherId).name("New Name").town("New Town").tlf(987654321).build();

            when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(existingSchool));
            when(schoolRepository.save(any(School.class))).thenReturn(savedSchool);

            final School result = schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(schoolId);
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getTown()).isEqualTo("New Town");
            assertThat(result.getTlf()).isEqualTo(987654321);
            verify(schoolRepository, times(1)).findById(schoolId);
            verify(schoolRepository, times(1)).save(existingSchool);
        }

        @Test
        void when_name_is_null_expect_school_validation_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final School updatedSchoolData = School.builder().name(null).build();
            final String expectedErrorMessage = "School name is required.";

            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolValidationException.class);
            final SchoolValidationException exception = (SchoolValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo(expectedErrorMessage);
            verify(schoolRepository, never()).findById(any());
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED), eq(null), any(Locale.class));
        }

        @Test
        void when_tlf_is_invalid_expect_school_validation_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final School updatedSchoolData = School.builder().name("Valid Name").tlf(123).build();
            final String expectedErrorMessage = "Telephone number must be 9 digits.";

            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolValidationException.class);
            final SchoolValidationException exception = (SchoolValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("tlf");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo(expectedErrorMessage);
            verify(schoolRepository, never()).findById(any());
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_VALIDATION_TLF_INVALID), eq(null), any(Locale.class));
        }

        @Test
        void when_school_does_not_exist_expect_school_not_found_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final School updatedSchoolData = School.builder().name("New Name").build();
            final String expectedErrorMessage = "School with ID 1 not found.";

            when(schoolRepository.findById(schoolId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolNotFoundException.class);
            final SchoolNotFoundException exception = (SchoolNotFoundException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(schoolRepository, times(1)).findById(schoolId);
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_NOT_FOUND), any(), any(Locale.class));
        }

        @Test
        void when_school_is_not_owned_by_teacher_expect_school_forbidden_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final Integer otherTeacherId = 999;
            final School existingSchool = School.builder().id(schoolId).teacherId(otherTeacherId).name("Old Name").build();
            final School updatedSchoolData = School.builder().name("New Name").build();
            final String expectedErrorMessage = "You are not authorized to update this school.";

            when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(existingSchool));
            when(messageSource.getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class)))
                    .thenReturn(expectedErrorMessage);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

            final ThrowingCallable call = () -> schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolForbiddenException.class);
            final SchoolForbiddenException exception = (SchoolForbiddenException) thrown;
            assertThat(exception.getErrorDescription()).isEqualTo(expectedErrorMessage);
            verify(schoolRepository, times(1)).findById(schoolId);
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(eq(MessageKeys.SCHOOL_FORBIDDEN), any(), any(Locale.class));
        }

        @Test
        void when_locale_is_used_for_validation_messages_expect_translated_validation_error() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final School updatedSchoolData = School.builder().name(null).build();
            final String expectedSpanishMessage = "El nombre del colegio es obligatorio.";
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH))
                    .thenReturn(expectedSpanishMessage);

            final ThrowingCallable call = () -> schoolService.updateSchool(schoolId, updatedSchoolData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(SchoolValidationException.class);
            final SchoolValidationException exception = (SchoolValidationException) thrown;
            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            assertThat(exception.getErrors().get(0).getMessage()).isEqualTo(expectedSpanishMessage);
            verify(schoolRepository, never()).findById(any());
            verify(schoolRepository, never()).save(any());
            verify(messageSource, times(1)).getMessage(MessageKeys.SCHOOL_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH);
        }
    }
}
