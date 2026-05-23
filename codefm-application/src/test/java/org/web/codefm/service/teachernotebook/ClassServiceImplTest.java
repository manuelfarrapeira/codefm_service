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
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.exception.teachernotebook.*;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;
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

    private ClassServiceImpl classService;

    @BeforeEach
    void beforeEach() {
        this.classService = new ClassServiceImpl(this.classRepository, this.schoolService, this.messageSource, this.sessionUser);
    }

    @Nested
    class GetActiveClassesBySchoolIdAndTeacherId {

        @Test
        void when_school_exists_and_teacher_owns_it_expect_classes() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class class1 = Class.builder().id(1).schoolId(schoolId).name("Math Class").schoolYear("24/25").build();
            final List<Class> expectedClasses = List.of(class1);

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(classRepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(expectedClasses);

            final List<Class> result = classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

            assertThat(result).isNotNull().hasSize(1);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, times(1)).findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        }

        @Test
        void when_school_does_not_exist_expect_school_not_found_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, Locale.ENGLISH)).thenReturn("School not found");

            final ThrowingCallable callable = () -> classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(SchoolNotFoundException.class);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).findActiveClassesBySchoolIdAndTeacherId(anyInt(), anyInt());
        }

        @Test
        void when_teacher_does_not_own_school_expect_school_forbidden_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final Integer differentTeacherId = 2;
            final School school = School.builder().id(schoolId).teacherId(differentTeacherId).name("School A").build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, Locale.ENGLISH)).thenReturn("Forbidden");

            final ThrowingCallable callable = () -> classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(SchoolForbiddenException.class);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).findActiveClassesBySchoolIdAndTeacherId(anyInt(), anyInt());
        }
    }

    @Nested
    class CreateClass {

        @Test
        void when_data_is_valid_expect_class_to_be_created() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            final Class createdClass = Class.builder()
                    .id(1)
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(classRepository.save(classToCreate)).thenReturn(createdClass);

            final Class result = classService.createClass(classToCreate, teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Math Class");
            assertThat(result.getSchoolYear()).isEqualTo("24/25");
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, times(1)).save(classToCreate);
        }

        @Test
        void when_name_is_empty_expect_class_validation_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("")
                    .schoolYear("24/25")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH))
                    .thenReturn("Class name is required.");

            final ThrowingCallable call = () -> classService.createClass(classToCreate, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(ClassValidationException.class);
            final ClassValidationException exception = (ClassValidationException) thrown;
            assertThat(exception.getErrors()).isNotNull().hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("name");
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_year_is_empty_expect_class_validation_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_REQUIRED, null, Locale.ENGLISH))
                    .thenReturn("School year is required.");

            final ThrowingCallable call = () -> classService.createClass(classToCreate, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(ClassValidationException.class);
            final ClassValidationException exception = (ClassValidationException) thrown;
            assertThat(exception.getErrors()).isNotNull().hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("schoolYear");
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_year_format_is_invalid_expect_class_validation_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("2024/2025")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_FORMAT_INVALID, null, Locale.ENGLISH))
                    .thenReturn("School year must be in format NN/NN (e.g., 24/25).");

            final ThrowingCallable call = () -> classService.createClass(classToCreate, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(ClassValidationException.class);
            final ClassValidationException exception = (ClassValidationException) thrown;
            assertThat(exception.getErrors()).isNotNull().hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("schoolYear");
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_year_is_not_consecutive_expect_class_validation_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/26")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_NOT_CONSECUTIVE, null, Locale.ENGLISH))
                    .thenReturn("School year numbers must be consecutive (e.g., 24/25).");

            final ThrowingCallable call = () -> classService.createClass(classToCreate, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(ClassValidationException.class);
            final ClassValidationException exception = (ClassValidationException) thrown;
            assertThat(exception.getErrors()).isNotNull().hasSize(1);
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("schoolYear");
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_is_not_found_expect_school_not_found_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(MessageKeys.SCHOOL_NOT_FOUND, null, Locale.ENGLISH))
                    .thenReturn("School not found");

            final ThrowingCallable callable = () -> classService.createClass(classToCreate, teacherId);

            assertThatThrownBy(callable).isInstanceOf(SchoolNotFoundException.class);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_is_not_owned_by_teacher_expect_school_forbidden_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final Integer differentTeacherId = 2;
            final School school = School.builder().id(schoolId).teacherId(differentTeacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("24/25")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.SCHOOL_FORBIDDEN, null, Locale.ENGLISH))
                    .thenReturn("Forbidden");

            final ThrowingCallable callable = () -> classService.createClass(classToCreate, teacherId);

            assertThatThrownBy(callable).isInstanceOf(SchoolForbiddenException.class);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_year_causes_number_format_exception_expect_class_validation_exception() throws Exception {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School school = School.builder().id(schoolId).teacherId(teacherId).name("School A").build();

            final Class classToCreate = Class.builder()
                    .schoolId(schoolId)
                    .name("Math Class")
                    .schoolYear("AB/CD")
                    .build();

            final java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            final sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

            final java.lang.reflect.Field patternField = ClassServiceImpl.class.getDeclaredField("SCHOOL_YEAR_PATTERN");
            final Object staticFieldBase = unsafe.staticFieldBase(patternField);
            final long staticFieldOffset = unsafe.staticFieldOffset(patternField);
            final Pattern originalPattern = (Pattern) unsafe.getObject(staticFieldBase, staticFieldOffset);
            unsafe.putObject(staticFieldBase, staticFieldOffset, Pattern.compile("^.{2}/.{2}$"));

            try {
                when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
                when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
                when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_FORMAT_INVALID, null, Locale.ENGLISH))
                        .thenReturn("School year must be in format NN/NN.");

                final ThrowingCallable call = () -> classService.createClass(classToCreate, teacherId);
                final Throwable thrown = catchThrowable(call);

                assertThat(thrown).isInstanceOf(ClassValidationException.class);
                final ClassValidationException exception = (ClassValidationException) thrown;
                assertThat(exception.getErrors()).isNotNull().hasSize(1);
                assertThat(exception.getErrors().get(0).getParam()).isEqualTo("schoolYear");
                verify(classRepository, never()).save(any());
            } finally {
                unsafe.putObject(staticFieldBase, staticFieldOffset, originalPattern);
            }
        }
    }

    @Nested
    class SoftDeleteClass {

        @Test
        void when_class_exists_and_school_is_owned_by_teacher_expect_repository_call() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Integer schoolId = 10;

            final Class clazz = Class.builder()
                    .id(classId)
                    .schoolId(schoolId)
                    .name("Test Class")
                    .schoolYear("24/25")
                    .build();

            final School school = School.builder()
                    .id(schoolId)
                    .teacherId(teacherId)
                    .name("Test School")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(classId)).thenReturn(Optional.of(clazz));
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(classRepository.softDeleteClass(classId, teacherId)).thenReturn(clazz);

            classService.softDeleteClass(classId, teacherId);

            verify(classRepository, times(1)).findById(classId);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, times(1)).softDeleteClass(classId, teacherId);
        }

        @Test
        void when_class_does_not_exist_expect_class_not_found_exception() {
            final Integer classId = 999;
            final Integer teacherId = 1;

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(classId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, Locale.ENGLISH))
                    .thenReturn("Class not found.");

            final ThrowingCallable callable = () -> classService.softDeleteClass(classId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(ClassNotFoundException.class);
            verify(classRepository, times(1)).findById(classId);
            verify(classRepository, never()).softDeleteClass(any(), any());
        }

        @Test
        void when_school_is_not_owned_by_teacher_expect_class_forbidden_exception() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Integer schoolId = 10;
            final Integer differentTeacherId = 999;

            final Class clazz = Class.builder()
                    .id(classId)
                    .schoolId(schoolId)
                    .name("Test Class")
                    .schoolYear("24/25")
                    .build();

            final School school = School.builder()
                    .id(schoolId)
                    .teacherId(differentTeacherId)
                    .name("Test School")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(classId)).thenReturn(Optional.of(clazz));
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, Locale.ENGLISH))
                    .thenReturn("You are not authorized to delete this class.");

            final ThrowingCallable callable = () -> classService.softDeleteClass(classId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(ClassForbiddenException.class);
            verify(classRepository, times(1)).findById(classId);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).softDeleteClass(any(), any());
        }

        @Test
        void when_school_is_not_found_expect_class_forbidden_exception() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Integer schoolId = 10;

            final Class clazz = Class.builder()
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

            final ThrowingCallable callable = () -> classService.softDeleteClass(classId, teacherId);

            assertThatThrownBy(callable).isInstanceOf(ClassForbiddenException.class);
            verify(classRepository, times(1)).findById(classId);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).softDeleteClass(any(), any());
        }
    }

    @Nested
    class UpdateClass {

        @Test
        void when_data_is_valid_and_teacher_owns_school_expect_class_to_be_updated() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Integer schoolId = 10;

            final Class existingClass = Class.builder()
                    .id(classId)
                    .schoolId(schoolId)
                    .name("Old Name")
                    .schoolYear("23/24")
                    .build();

            final Class updateData = Class.builder()
                    .name("New Name")
                    .schoolYear("24/25")
                    .build();

            final Class updatedClass = Class.builder()
                    .id(classId)
                    .schoolId(schoolId)
                    .name("New Name")
                    .schoolYear("24/25")
                    .build();

            final School school = School.builder()
                    .id(schoolId)
                    .teacherId(teacherId)
                    .name("Test School")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(classId)).thenReturn(Optional.of(existingClass));
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(classRepository.save(any(Class.class))).thenReturn(updatedClass);

            final Class result = classService.updateClass(classId, updateData, teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getSchoolYear()).isEqualTo("24/25");
            verify(classRepository, times(1)).findById(classId);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, times(1)).save(any(Class.class));
        }

        @Test
        void when_class_does_not_exist_expect_class_not_found_exception() {
            final Integer classId = 999;
            final Integer teacherId = 1;
            final Class updateData = Class.builder().name("New Name").schoolYear("24/25").build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(classId)).thenReturn(Optional.empty());
            when(messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, Locale.ENGLISH))
                    .thenReturn("Class not found.");

            final ThrowingCallable callable = () -> classService.updateClass(classId, updateData, teacherId);

            assertThatThrownBy(callable).isInstanceOf(ClassNotFoundException.class);
            verify(classRepository, times(1)).findById(classId);
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_name_is_empty_expect_class_validation_exception() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Class updateData = Class.builder().name("").schoolYear("24/25").build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_NAME_REQUIRED, null, Locale.ENGLISH))
                    .thenReturn("Class name is required.");

            final ThrowingCallable call = () -> classService.updateClass(classId, updateData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(ClassValidationException.class);
            final ClassValidationException exception = (ClassValidationException) thrown;
            assertThat(exception.getErrors()).isNotNull().hasSize(1);
            verify(classRepository, never()).findById(any());
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_school_year_is_invalid_expect_class_validation_exception() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Class updateData = Class.builder().name("Valid Name").schoolYear("23/25").build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(MessageKeys.CLASS_VALIDATION_SCHOOL_YEAR_NOT_CONSECUTIVE, null, Locale.ENGLISH))
                    .thenReturn("School year numbers must be consecutive.");

            final ThrowingCallable call = () -> classService.updateClass(classId, updateData, teacherId);
            final Throwable thrown = catchThrowable(call);

            assertThat(thrown).isInstanceOf(ClassValidationException.class);
            final ClassValidationException exception = (ClassValidationException) thrown;
            assertThat(exception.getErrors()).isNotNull();
            verify(classRepository, never()).findById(any());
            verify(classRepository, never()).save(any());
        }

        @Test
        void when_teacher_does_not_own_school_expect_class_forbidden_exception() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Integer schoolId = 10;
            final Integer differentTeacherId = 999;

            final Class existingClass = Class.builder()
                    .id(classId)
                    .schoolId(schoolId)
                    .name("Old Name")
                    .schoolYear("23/24")
                    .build();

            final Class updateData = Class.builder()
                    .name("New Name")
                    .schoolYear("24/25")
                    .build();

            final School school = School.builder()
                    .id(schoolId)
                    .teacherId(differentTeacherId)
                    .name("Test School")
                    .build();

            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(classId)).thenReturn(Optional.of(existingClass));
            when(schoolService.getSchoolById(schoolId)).thenReturn(Optional.of(school));
            when(messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, Locale.ENGLISH))
                    .thenReturn("You are not authorized to update this class.");

            final ThrowingCallable callable = () -> classService.updateClass(classId, updateData, teacherId);

            assertThatThrownBy(callable).isInstanceOf(ClassForbiddenException.class);
            verify(classRepository, times(1)).findById(classId);
            verify(schoolService, times(1)).getSchoolById(schoolId);
            verify(classRepository, never()).save(any());
        }
    }
}

