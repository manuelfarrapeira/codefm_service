package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.*;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceValidationException;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentAbsenceServiceImplTest {

    @Mock
    private StudentAbsenceRepository studentAbsenceRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private SubjectClassRepository subjectClassRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private StudentAbsenceServiceImpl studentAbsenceService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer STUDENT_ID = 20;
    private static final Integer SUBJECT_ID = 30;
    private static final Integer ABSENCE_ID = 100;
    private static final Integer STUDENT_CLASS_ID = 50;
    private static final LocalDate DATE = LocalDate.of(2025, 3, 17);

    @BeforeEach
    void beforeEach() {
        studentAbsenceService = new StudentAbsenceServiceImpl(
                studentAbsenceRepository, classRepository, studentRepository,
                studentClassRepository, subjectRepository, subjectClassRepository,
                scheduleRepository, messageSource, sessionUser);
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error message");
    }

    @Nested
    class CreateAbsences {

        @Test
        void when_subject_id_provided_expect_create_absences() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(student));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            final Subject subject = Subject.builder().id(SUBJECT_ID).build();
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID))
                    .thenReturn(true);
            when(scheduleRepository.existsByClassIdAndSubjectIdAndDay(CLASS_ID, SUBJECT_ID, DATE.getDayOfWeek().getValue()))
                    .thenReturn(true);
            when(studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, SUBJECT_ID, DATE))
                    .thenReturn(false);

            final StudentAbsence saved = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
                    .studentId(STUDENT_ID).classId(CLASS_ID).subjectId(SUBJECT_ID).absenceDate(DATE).build();
            when(studentAbsenceRepository.saveAll(anyList())).thenReturn(List.of(saved));
            when(studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE)).thenReturn(List.of(saved));

            final List<StudentAbsence> result = studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE);

            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getId()).isEqualTo(ABSENCE_ID);
            verify(studentAbsenceRepository).saveAll(anyList());
        }

        @Test
        void when_subject_id_is_null_expect_create_absences_from_schedule() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(student));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            final int dayOfWeek = DATE.getDayOfWeek().getValue();
            when(scheduleRepository.findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek)).thenReturn(List.of(30, 31));
            when(studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 30, DATE))
                    .thenReturn(false);
            when(studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 31, DATE))
                    .thenReturn(false);

            final StudentAbsence saved1 = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
                    .studentId(STUDENT_ID).classId(CLASS_ID).subjectId(30).absenceDate(DATE).build();
            final StudentAbsence saved2 = StudentAbsence.builder().id(101).studentClassId(STUDENT_CLASS_ID)
                    .studentId(STUDENT_ID).classId(CLASS_ID).subjectId(31).absenceDate(DATE).build();
            when(studentAbsenceRepository.saveAll(anyList())).thenReturn(List.of(saved1, saved2));
            when(studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE))
                    .thenReturn(List.of(saved1, saved2));

            final List<StudentAbsence> result = studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, null, DATE);

            assertThat(result).hasSize(2);
            verify(scheduleRepository).findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek);
            verify(studentAbsenceRepository).saveAll(anyList());
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#classNotFoundSetups")
        void when_class_not_exists_expect_throw_class_not_found_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE);

            assertThatThrownBy(call).isInstanceOf(ClassNotFoundException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#classForbiddenSetups")
        void when_class_not_owned_by_teacher_expect_throw_class_forbidden_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE);

            assertThatThrownBy(call).isInstanceOf(ClassForbiddenException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#studentValidationSetups")
        void when_student_is_invalid_expect_throw_validation_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE);
            final StudentAbsenceValidationException exception = catchThrowableOfType(call, StudentAbsenceValidationException.class);

            assertThat(exception.getErrors()).isNotEmpty();
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("studentId");
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#subjectValidationSetups")
        void when_subject_is_invalid_expect_throw_validation_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(student));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, SUBJECT_ID, DATE);
            final StudentAbsenceValidationException exception = catchThrowableOfType(call, StudentAbsenceValidationException.class);

            assertThat(exception.getErrors()).isNotEmpty();
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("subjectId");
        }

        @Test
        void when_no_subjects_scheduled_on_day_expect_throw_validation_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(student));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            when(scheduleRepository.findSubjectIdsByClassIdAndDay(CLASS_ID, DATE.getDayOfWeek().getValue()))
                    .thenReturn(List.of());

            final ThrowingCallable call = () -> studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, null, DATE);
            final StudentAbsenceValidationException exception = catchThrowableOfType(call, StudentAbsenceValidationException.class);

            assertThat(exception.getErrors()).isNotEmpty();
            assertThat(exception.getErrors().get(0).getParam()).isEqualTo("date");
        }

        @Test
        void when_absences_already_exist_expect_skip_duplicates() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(STUDENT_ID).build();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(student));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            final int dayOfWeek = DATE.getDayOfWeek().getValue();
            when(scheduleRepository.findSubjectIdsByClassIdAndDay(CLASS_ID, dayOfWeek)).thenReturn(List.of(30, 31));
            when(studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 30, DATE))
                    .thenReturn(true);
            when(studentAbsenceRepository.existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, 31, DATE))
                    .thenReturn(true);

            final StudentAbsence existing = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
                    .studentId(STUDENT_ID).classId(CLASS_ID).subjectId(30).absenceDate(DATE).build();
            when(studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE))
                    .thenReturn(List.of(existing));

            final List<StudentAbsence> result = studentAbsenceService.createAbsences(CLASS_ID, STUDENT_ID, null, DATE);

            assertThat(result).isNotEmpty();
            verify(studentAbsenceRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    class GetAbsences {

        @Test
        void when_student_id_provided_expect_return_absences() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
                    .studentId(STUDENT_ID).classId(CLASS_ID).build();
            when(studentAbsenceRepository.findByStudentClassId(STUDENT_CLASS_ID)).thenReturn(List.of(absence));

            final List<StudentAbsence> result = studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, null);

            assertThat(result).hasSize(1);
            verify(studentAbsenceRepository).findByStudentClassId(STUDENT_CLASS_ID);
        }

        @Test
        void when_date_provided_expect_return_absences() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).classId(CLASS_ID).absenceDate(DATE).build();
            when(studentAbsenceRepository.findByClassIdAndDate(CLASS_ID, DATE)).thenReturn(List.of(absence));

            final List<StudentAbsence> result = studentAbsenceService.getAbsences(CLASS_ID, null, DATE);

            assertThat(result).hasSize(1);
            verify(studentAbsenceRepository).findByClassIdAndDate(CLASS_ID, DATE);
        }

        @Test
        void when_both_student_id_and_date_provided_expect_return_absences() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).studentClassId(STUDENT_CLASS_ID)
                    .studentId(STUDENT_ID).classId(CLASS_ID).absenceDate(DATE).build();
            when(studentAbsenceRepository.findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE)).thenReturn(List.of(absence));

            final List<StudentAbsence> result = studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, DATE);

            assertThat(result).hasSize(1);
            verify(studentAbsenceRepository).findByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE);
        }

        @Test
        void when_neither_student_id_nor_date_provided_expect_return_all_absences() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final List<StudentAbsence> expected = List.of(StudentAbsence.builder().id(1).build());
            when(studentAbsenceRepository.findByClassId(CLASS_ID)).thenReturn(expected);

            final List<StudentAbsence> result = studentAbsenceService.getAbsences(CLASS_ID, null, null);

            assertThat(result).isEqualTo(expected);
            verify(studentAbsenceRepository).findByClassId(CLASS_ID);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#classNotFoundSetups")
        void when_class_not_exists_expect_throw_class_not_found_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, DATE);

            assertThatThrownBy(call).isInstanceOf(ClassNotFoundException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#classForbiddenSetups")
        void when_class_not_owned_by_teacher_expect_throw_class_forbidden_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.getAbsences(CLASS_ID, STUDENT_ID, DATE);

            assertThatThrownBy(call).isInstanceOf(ClassForbiddenException.class);
        }
    }

    @Nested
    class DeleteAbsence {

        @Test
        void when_absence_exists_and_owned_by_teacher_expect_delete_absence() {
            final StudentAbsence absence = StudentAbsence.builder().id(ABSENCE_ID).build();
            when(studentAbsenceRepository.findByIdAndTeacherId(ABSENCE_ID, TEACHER_ID)).thenReturn(Optional.of(absence));

            studentAbsenceService.deleteAbsence(ABSENCE_ID);

            verify(studentAbsenceRepository).deleteById(ABSENCE_ID);
        }

        @Test
        void when_absence_not_found_expect_throw_not_found_exception() {
            when(studentAbsenceRepository.findByIdAndTeacherId(ABSENCE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable call = () -> studentAbsenceService.deleteAbsence(ABSENCE_ID);

            assertThatThrownBy(call).isInstanceOf(StudentAbsenceNotFoundException.class);
        }
    }

    @Nested
    class DeleteAbsencesByStudentAndDate {

        @Test
        void when_class_owned_by_teacher_expect_delete_absences() {
            final Class clazz = Class.builder().id(CLASS_ID).build();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));

            final StudentClass sc = StudentClass.builder().id(STUDENT_CLASS_ID).classId(CLASS_ID).studentId(STUDENT_ID).build();
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID)).thenReturn(Optional.of(sc));

            studentAbsenceService.deleteAbsencesByStudentAndDate(CLASS_ID, STUDENT_ID, DATE);

            verify(studentAbsenceRepository).deleteByStudentClassIdAndDate(STUDENT_CLASS_ID, DATE);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#classNotFoundSetups")
        void when_class_not_exists_expect_throw_class_not_found_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.deleteAbsencesByStudentAndDate(CLASS_ID, STUDENT_ID, DATE);

            assertThatThrownBy(call).isInstanceOf(ClassNotFoundException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.StudentAbsenceServiceImplTest#classForbiddenSetups")
        void when_class_not_owned_by_teacher_expect_throw_class_forbidden_exception(Consumer<StudentAbsenceServiceImplTest> setup) {
            setup.accept(StudentAbsenceServiceImplTest.this);

            final ThrowingCallable call = () -> StudentAbsenceServiceImplTest.this.studentAbsenceService.deleteAbsencesByStudentAndDate(CLASS_ID, STUDENT_ID, DATE);

            assertThatThrownBy(call).isInstanceOf(ClassForbiddenException.class);
        }
    }

    static Stream<Consumer<StudentAbsenceServiceImplTest>> classNotFoundSetups() {
        return Stream.of(
                t -> when(t.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty())
        );
    }

    static Stream<Consumer<StudentAbsenceServiceImplTest>> classForbiddenSetups() {
        return Stream.of(
                t -> {
                    when(t.classRepository.findById(CLASS_ID))
                            .thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
                    when(t.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                            .thenReturn(Optional.empty());
                }
        );
    }

    static Stream<Consumer<StudentAbsenceServiceImplTest>> studentValidationSetups() {
        return Stream.of(
                t -> when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                        .thenReturn(Optional.empty()),
                t -> {
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
                    when(t.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                            .thenReturn(Optional.empty());
                },
                t -> {
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
                    when(t.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                            .thenReturn(Optional.of(StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID)
                                    .deletionDate(LocalDate.now()).build()));
                }
        );
    }

    static Stream<Consumer<StudentAbsenceServiceImplTest>> subjectValidationSetups() {
        return Stream.of(
                t -> when(t.subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID))
                        .thenReturn(Optional.empty()),
                t -> {
                    when(t.subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Subject.builder().id(SUBJECT_ID).build()));
                    when(t.subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID))
                            .thenReturn(false);
                },
                t -> {
                    when(t.subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Subject.builder().id(SUBJECT_ID).build()));
                    when(t.subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID))
                            .thenReturn(true);
                    when(t.scheduleRepository.existsByClassIdAndSubjectIdAndDay(CLASS_ID, SUBJECT_ID,
                            DATE.getDayOfWeek().getValue())).thenReturn(false);
                }
        );
    }
}

