package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private StudentGroupServiceImpl studentGroupService;

    @ParameterizedTest
    @MethodSource("groupSizeTestCases")
    void generateGroups_shouldReturnExpectedGroupSizes(int studentCount, List<Integer> expectedSizes) {
        final List<Student> students = buildStudents(studentCount);
        setupMocks(students);

        final List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, true);

        assertEquals(expectedSizes.size(), groups.size());
        for (int i = 0; i < expectedSizes.size(); i++) {
            assertEquals(expectedSizes.get(i), groups.get(i).size());
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
    void generateGroups_shouldReturnOneGroupOfThree_whenThreeStudents() {
        List<Student> students = buildStudents(3);
        setupMocks(students);

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, true);

        assertEquals(1, groups.size());
        assertEquals(3, groups.get(0).size());
    }

    @Test
    void generateGroups_shouldHaveAtMostOneCirclePerGroup_whenPrioritizeShapeDiversityTrue() {
        List<Student> students = List.of(
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

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, true);

        for (List<Integer> group : groups) {
            long circleCount = group.stream()
                    .map(id -> students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .filter(s -> "CIRCLE".equals(s.getShape()))
                    .count();
            assertTrue(circleCount <= 1);
        }
    }

    @Test
    void generateGroups_shouldHaveAtMostOneCirclePerGroup_whenPrioritizeShapeDiversityFalse() {
        List<Student> students = List.of(
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

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, false);

        for (List<Integer> group : groups) {
            long circleCount = group.stream()
                    .map(id -> students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .filter(s -> "CIRCLE".equals(s.getShape()))
                    .count();
            assertTrue(circleCount <= 1);
        }
    }

    @Test
    void generateGroups_shouldThrowStudentGroupValidationException_whenStudentHasNoShape() {
        List<Student> students = List.of(
                Student.builder().id(1).name("Ana").surnames("Garcia").gender("F").shape("CIRCLE").build(),
                Student.builder().id(2).name("Pedro").surnames("Lopez").gender("M").shape(null).build(),
                Student.builder().id(3).name("Maria").surnames("Ruiz").gender("F").shape("TRIANGLE").build(),
                Student.builder().id(4).name("Juan").surnames("Perez").gender("M").shape("SQUARE").build()
        );

        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID))
                .thenReturn(students.stream().map(Student::getId).collect(Collectors.toList()));
        when(this.studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(any(), eq(TEACHER_ID)))
                .thenReturn(students);
        when(this.messageSource.getMessage(eq(MessageKeys.STUDENT_GROUP_MISSING_SHAPE), any(), any(Locale.class)))
                .thenReturn("All students must have a shape assigned. Students without shape: Pedro Lopez.");

        StudentGroupValidationException exception = assertThrows(StudentGroupValidationException.class,
                () -> this.studentGroupService.generateGroups(CLASS_ID, true));

        assertFalse(exception.getErrors().isEmpty());
        assertEquals("shape", exception.getErrors().get(0).getParam());
    }

    @Test
    void generateGroups_shouldThrowStudentGroupValidationException_whenLessThanThreeStudents() {
        List<Student> students = List.of(
                Student.builder().id(1).name("A").surnames("A").gender("M").shape("CIRCLE").build(),
                Student.builder().id(2).name("B").surnames("B").gender("F").shape("SQUARE").build()
        );
        setupMocks(students);
        when(this.messageSource.getMessage(eq(MessageKeys.STUDENT_GROUP_MIN_STUDENTS), any(), any(Locale.class)))
                .thenReturn("At least 3 students are required.");

        StudentGroupValidationException exception = assertThrows(StudentGroupValidationException.class,
                () -> this.studentGroupService.generateGroups(CLASS_ID, true));

        assertFalse(exception.getErrors().isEmpty());
        assertEquals("classId", exception.getErrors().get(0).getParam());
    }

    @Test
    void generateGroups_shouldThrowStudentGroupValidationException_whenExactlyFiveStudents() {
        List<Student> students = buildStudents(5);
        setupMocks(students);
        when(this.messageSource.getMessage(eq(MessageKeys.STUDENT_GROUP_IMPOSSIBLE_COUNT), any(), any(Locale.class)))
                .thenReturn("Cannot form groups of 3 or 4 with 5 students.");

        StudentGroupValidationException exception = assertThrows(StudentGroupValidationException.class,
                () -> this.studentGroupService.generateGroups(CLASS_ID, true));

        assertFalse(exception.getErrors().isEmpty());
    }

    @Test
    void generateGroups_shouldThrowClassNotFoundException_whenClassNotFound() {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found.");

        assertThrows(ClassNotFoundException.class,
                () -> this.studentGroupService.generateGroups(CLASS_ID, true));
    }

    @Test
    void generateGroups_shouldContainAllStudentIds_whenGroupsGenerated() {
        List<Student> students = buildStudents(8);
        setupMocks(students);

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, true);

        List<Integer> allIds = groups.stream().flatMap(List::stream).collect(Collectors.toList());
        assertEquals(8, allIds.size());

        Set<Integer> expectedIds = students.stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(expectedIds, new HashSet<>(allIds));
    }

    @Test
    void generateGroups_shouldReturnThreeGroupsOfThree_whenNineStudents() {
        List<Student> students = buildStudents(9);
        setupMocks(students);

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, true);

        assertEquals(3, groups.size());
        for (List<Integer> group : groups) {
            assertEquals(3, group.size());
        }
    }

    @Test
    void generateGroups_shouldContainAllStudentIds_whenPrioritizeGenderDiversity() {
        List<Student> students = buildStudents(8);
        setupMocks(students);

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, false);

        List<Integer> allIds = groups.stream().flatMap(List::stream).collect(Collectors.toList());
        assertEquals(8, allIds.size());

        Set<Integer> expectedIds = students.stream().map(Student::getId).collect(Collectors.toSet());
        assertEquals(expectedIds, new HashSet<>(allIds));
    }

    @Test
    void generateGroups_shouldDefaultToShapePriority_whenNullParameter() {
        List<Student> students = buildStudents(4);
        setupMocks(students);

        List<List<Integer>> groupsNull = this.studentGroupService.generateGroups(CLASS_ID, null);

        setupMocks(students);

        List<List<Integer>> groupsTrue = this.studentGroupService.generateGroups(CLASS_ID, true);

        assertEquals(groupsTrue, groupsNull);
    }

    @Test
    void generateGroups_shouldReturnCorrectGroupSizes_whenPrioritizeGenderDiversity() {
        List<Student> students = buildStudents(7);
        setupMocks(students);

        List<List<Integer>> groups = this.studentGroupService.generateGroups(CLASS_ID, false);

        assertEquals(2, groups.size());
        assertEquals(4, groups.get(0).size());
        assertEquals(3, groups.get(1).size());
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
        String[] shapes = {"CIRCLE", "SQUARE", "TRIANGLE"};
        String[] genders = {"M", "F"};
        List<Student> students = new ArrayList<>();
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
}

