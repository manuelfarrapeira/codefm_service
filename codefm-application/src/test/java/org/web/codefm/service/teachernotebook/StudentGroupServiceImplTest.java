package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentGroupValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentGroupServiceImplTest {

    private static final Integer CLASS_ID = 1;
    private static final Integer TEACHER_ID = 10;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private StudentGroupServiceImpl studentGroupService;

    @BeforeEach
    void beforeEach() {
        studentGroupService = new StudentGroupServiceImpl(
                classRepository, studentClassRepository, studentRepository, messageSource, sessionUser);
    }

    private void setupMocks(List<Student> students) {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID))
                .thenReturn(students.stream().map(Student::getId).collect(Collectors.toList()));
        when(this.studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(any(), eq(TEACHER_ID)))
                .thenReturn(students);
    }

    private List<Student> buildStudents(int count) {
        final String[] shapes = {"CIRCLE", "SQUARE", "TRIANGLE"};
        final String[] genders = {"M", "F"};
        final List<Student> students = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            students.add(Student.builder()
                    .id(i)
                    .name("Name" + i)
                    .surnames("Surnames" + i)
                    .gender(genders[i % 2])
                    .shape(shapes[i % 3])
                    .build());
        }
        return students;
    }

    @Nested
    class GenerateGroups {

        @ParameterizedTest
        @MethodSource("groupSizeTestCases")
        void when_student_count_matches_expect_return_expected_group_sizes(int studentCount, List<Integer> expectedSizes) {
            final List<Student> students = buildStudents(studentCount);
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, true);

            assertThat(groups).hasSize(expectedSizes.size());
            for (int i = 0; i < expectedSizes.size(); i++) {
                assertThat(groups.get(i)).hasSize(expectedSizes.get(i));
            }
        }

        static Stream<Arguments> groupSizeTestCases() {
            return Stream.of(
                    Arguments.of(4, List.of(4)),
                    Arguments.of(8, List.of(4, 4)),
                    Arguments.of(7, List.of(4, 3))
            );
        }

        @Test
        void when_three_students_expect_return_one_group_of_three() {
            final List<Student> students = buildStudents(3);
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, true);

            assertThat(groups).hasSize(1);
            assertThat(groups.get(0)).hasSize(3);
        }

        @Test
        void when_prioritize_shape_diversity_true_expect_have_at_most_one_circle_per_group() {
            final List<Student> students = List.of(
                    Student.builder().id(1).name("A").surnames("A").gender("M").shape("CIRCLE").build(),
                    Student.builder().id(2).name("B").surnames("B").gender("F").shape("SQUARE").build(),
                    Student.builder().id(3).name("C").surnames("C").gender("M").shape("TRIANGLE").build(),
                    Student.builder().id(4).name("D").surnames("D").gender("F").shape("SQUARE").build(),
                    Student.builder().id(5).name("E").surnames("E").gender("M").shape("CIRCLE").build(),
                    Student.builder().id(6).name("F").surnames("F").gender("F").shape("TRIANGLE").build(),
                    Student.builder().id(7).name("G").surnames("G").gender("M").shape("SQUARE").build(),
                    Student.builder().id(8).name("H").surnames("H").gender("F").shape("TRIANGLE").build()
            );
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, true);

            for (final List<Integer> group : groups) {
                final long circleCount = group.stream()
                        .map(id -> students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .filter(s -> "CIRCLE".equals(s.getShape()))
                        .count();
                assertThat(circleCount).isLessThanOrEqualTo(1);
            }
        }

        @Test
        void when_prioritize_shape_diversity_false_expect_have_at_most_one_circle_per_group() {
            final List<Student> students = List.of(
                    Student.builder().id(1).name("A").surnames("A").gender("M").shape("CIRCLE").build(),
                    Student.builder().id(2).name("B").surnames("B").gender("F").shape("SQUARE").build(),
                    Student.builder().id(3).name("C").surnames("C").gender("M").shape("TRIANGLE").build(),
                    Student.builder().id(4).name("D").surnames("D").gender("F").shape("SQUARE").build(),
                    Student.builder().id(5).name("E").surnames("E").gender("M").shape("CIRCLE").build(),
                    Student.builder().id(6).name("F").surnames("F").gender("F").shape("TRIANGLE").build(),
                    Student.builder().id(7).name("G").surnames("G").gender("M").shape("SQUARE").build(),
                    Student.builder().id(8).name("H").surnames("H").gender("F").shape("TRIANGLE").build()
            );
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, false);

            for (final List<Integer> group : groups) {
                final long circleCount = group.stream()
                        .map(id -> students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .filter(s -> "CIRCLE".equals(s.getShape()))
                        .count();
                assertThat(circleCount).isLessThanOrEqualTo(1);
            }
        }

        @Test
        void when_student_has_no_shape_expect_throw_validation_exception() {
            final List<Student> students = List.of(
                    Student.builder().id(1).name("Ana").surnames("Garcia").gender("F").shape("CIRCLE").build(),
                    Student.builder().id(2).name("Pedro").surnames("Lopez").gender("M").shape(null).build(),
                    Student.builder().id(3).name("Maria").surnames("Ruiz").gender("F").shape("TRIANGLE").build(),
                    Student.builder().id(4).name("Juan").surnames("Perez").gender("M").shape("SQUARE").build()
            );

            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID))
                    .thenReturn(students.stream().map(Student::getId).collect(Collectors.toList()));
            when(studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(any(), eq(TEACHER_ID)))
                    .thenReturn(students);
            when(messageSource.getMessage(eq(MessageKeys.STUDENT_GROUP_MISSING_SHAPE), any(), any(Locale.class)))
                    .thenReturn("All students must have a shape assigned. Students without shape: Pedro Lopez.");

            final ThrowingCallable action = () -> studentGroupService.generateGroups(CLASS_ID, true);
            final StudentGroupValidationException ex = catchThrowableOfType(action, StudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("shape");
        }

        @Test
        void when_less_than_three_students_expect_throw_validation_exception() {
            final List<Student> students = List.of(
                    Student.builder().id(1).name("A").surnames("A").gender("M").shape("CIRCLE").build(),
                    Student.builder().id(2).name("B").surnames("B").gender("F").shape("SQUARE").build()
            );
            setupMocks(students);
            when(messageSource.getMessage(eq(MessageKeys.STUDENT_GROUP_MIN_STUDENTS), any(), any(Locale.class)))
                    .thenReturn("At least 3 students are required.");

            final ThrowingCallable action = () -> studentGroupService.generateGroups(CLASS_ID, true);
            final StudentGroupValidationException ex = catchThrowableOfType(action, StudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("classId");
        }

        @Test
        void when_exactly_five_students_expect_throw_validation_exception() {
            final List<Student> students = buildStudents(5);
            setupMocks(students);
            when(messageSource.getMessage(eq(MessageKeys.STUDENT_GROUP_IMPOSSIBLE_COUNT), any(), any(Locale.class)))
                    .thenReturn("Cannot form groups of 3 or 4 with 5 students.");

            final ThrowingCallable action = () -> studentGroupService.generateGroups(CLASS_ID, true);
            assertThatThrownBy(action).isInstanceOf(StudentGroupValidationException.class);
        }

        @Test
        void when_class_not_found_expect_throw_class_not_found_exception() {
            when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found.");

            final ThrowingCallable action = () -> studentGroupService.generateGroups(CLASS_ID, true);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        void when_groups_generated_expect_contain_all_student_ids() {
            final List<Student> students = buildStudents(8);
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, true);

            final List<Integer> allIds = groups.stream().flatMap(List::stream).collect(Collectors.toList());
            assertThat(allIds).hasSize(8);

            final Set<Integer> expectedIds = students.stream().map(Student::getId).collect(Collectors.toSet());
            assertThat(new HashSet<>(allIds)).isEqualTo(expectedIds);
        }

        @Test
        void when_nine_students_expect_return_three_groups_of_three() {
            final List<Student> students = buildStudents(9);
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, true);

            assertThat(groups).hasSize(3);
            for (final List<Integer> group : groups) {
                assertThat(group).hasSize(3);
            }
        }

        @Test
        void when_prioritize_gender_diversity_expect_contain_all_student_ids() {
            final List<Student> students = buildStudents(8);
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, false);

            final List<Integer> allIds = groups.stream().flatMap(List::stream).collect(Collectors.toList());
            assertThat(allIds).hasSize(8);

            final Set<Integer> expectedIds = students.stream().map(Student::getId).collect(Collectors.toSet());
            assertThat(new HashSet<>(allIds)).isEqualTo(expectedIds);
        }

        @Test
        void when_null_parameter_expect_default_to_shape_priority() {
            final List<Student> students = buildStudents(4);
            setupMocks(students);

            final List<List<Integer>> groupsNull = studentGroupService.generateGroups(CLASS_ID, null);

            setupMocks(students);

            final List<List<Integer>> groupsTrue = studentGroupService.generateGroups(CLASS_ID, true);

            assertThat(groupsNull).isEqualTo(groupsTrue);
        }

        @Test
        void when_prioritize_gender_diversity_expect_return_correct_group_sizes() {
            final List<Student> students = buildStudents(7);
            setupMocks(students);

            final List<List<Integer>> groups = studentGroupService.generateGroups(CLASS_ID, false);

            assertThat(groups).hasSize(2);
            assertThat(groups.get(0)).hasSize(4);
            assertThat(groups.get(1)).hasSize(3);
        }
    }
}

