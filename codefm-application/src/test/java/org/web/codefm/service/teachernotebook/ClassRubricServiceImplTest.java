package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.*;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.exception.teachernotebook.ClassRubricNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ClassRubricValidationException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassRubricCriteriaNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentClassRubricCriteriaValidationException;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassRubricServiceImplTest {

    @Mock
    private ClassRubricRepository classRubricRepository;
    @Mock
    private StudentClassRubricCriteriaRepository studentClassRubricCriteriaRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private SkillRubricRepository skillRubricRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentClassRepository studentClassRepository;
    @Mock
    private SkillRubricCriteriaRepository skillRubricCriteriaRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    @InjectMocks
    private ClassRubricServiceImpl classRubricService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer RUBRIC_ID = 20;
    private static final Integer CLASS_RUBRIC_ID = 30;
    private static final Integer STUDENT_ID = 40;
    private static final Integer CRITERION_ID = 50;
    private static final Integer SKILL_ID = 60;
    private static final Integer CRITERIA_ID = 70;

    private void setupSession() {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    private void setupClassOwnership() {
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
    }

    private void setupClassRubricOwnership() {
        when(this.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                .thenReturn(Optional.of(ClassRubric.builder()
                        .id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build()));
    }

    @Test
    void getRubricsByClassId_shouldReturnRubrics_whenClassBelongsToTeacher() {
        this.setupSession();
        this.setupClassOwnership();
        final List<ClassRubric> expected = List.of(
                ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build());
        when(this.classRubricRepository.findByClassId(CLASS_ID)).thenReturn(expected);

        final List<ClassRubric> result = this.classRubricService.getRubricsByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(this.classRubricRepository).findByClassId(CLASS_ID);
    }

    @Test
    void getRubricsByClassId_shouldThrowClassRubricNotFoundException_whenClassNotOwnedByTeacher() {
        this.setupSession();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");

        assertThrows(ClassRubricNotFoundException.class,
                () -> this.classRubricService.getRubricsByClassId(CLASS_ID));
    }

    @Test
    void assignRubricToClass_shouldSaveAssignment_whenAllValidationsPass() {
        this.setupSession();
        this.setupClassOwnership();
        when(this.skillRubricRepository.findById(RUBRIC_ID))
                .thenReturn(Optional.of(SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).build()));
        when(this.skillRepository.findByIdAndTeacherId(SKILL_ID, TEACHER_ID))
                .thenReturn(Optional.of(Skill.builder().id(SKILL_ID).teacherId(TEACHER_ID).build()));
        when(this.classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(CLASS_ID, RUBRIC_ID))
                .thenReturn(false);
        final ClassRubric saved = ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build();
        when(this.classRubricRepository.save(any(ClassRubric.class))).thenReturn(saved);

        final ClassRubric result = this.classRubricService.assignRubricToClass(CLASS_ID, RUBRIC_ID);

        assertNotNull(result);
        assertEquals(CLASS_RUBRIC_ID, result.getId());
        verify(this.classRubricRepository).save(any(ClassRubric.class));
    }

    @ParameterizedTest
    @MethodSource("assignRubricToClassValidationSetups")
    void assignRubricToClass_shouldThrowClassRubricValidationException_whenValidationFails(
            Consumer<ClassRubricServiceImplTest> setup) {
        this.setupSession();
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
        setup.accept(this);

        assertThrows(ClassRubricValidationException.class,
                () -> this.classRubricService.assignRubricToClass(CLASS_ID, RUBRIC_ID));
    }

    static Stream<Consumer<ClassRubricServiceImplTest>> assignRubricToClassValidationSetups() {
        return Stream.of(
                t -> when(t.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                        .thenReturn(Optional.empty()),
                t -> {
                    when(t.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
                    when(t.skillRubricRepository.findById(RUBRIC_ID)).thenReturn(Optional.empty());
                },
                t -> {
                    when(t.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
                    when(t.skillRubricRepository.findById(RUBRIC_ID))
                            .thenReturn(Optional.of(SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).build()));
                    when(t.skillRepository.findByIdAndTeacherId(SKILL_ID, TEACHER_ID))
                            .thenReturn(Optional.empty());
                },
                t -> {
                    when(t.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
                    when(t.skillRubricRepository.findById(RUBRIC_ID))
                            .thenReturn(Optional.of(SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).build()));
                    when(t.skillRepository.findByIdAndTeacherId(SKILL_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Skill.builder().id(SKILL_ID).build()));
                    when(t.classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(CLASS_ID, RUBRIC_ID))
                            .thenReturn(true);
                }
        );
    }

    @Test
    void removeRubricFromClass_shouldSoftDelete_whenClassRubricBelongsToTeacher() {
        this.setupSession();
        this.setupClassRubricOwnership();

        this.classRubricService.removeRubricFromClass(CLASS_RUBRIC_ID);

        verify(this.classRubricRepository).softDeleteById(CLASS_RUBRIC_ID);
    }

    @Test
    void removeRubricFromClass_shouldThrowClassRubricNotFoundException_whenNotOwnedByTeacher() {
        this.setupSession();
        when(this.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");

        assertThrows(ClassRubricNotFoundException.class,
                () -> this.classRubricService.removeRubricFromClass(CLASS_RUBRIC_ID));
    }

    @Test
    void getAllStudentCriteriaByClassId_shouldReturnGroupedList_whenClassBelongsToTeacher() {
        this.setupSession();
        this.setupClassOwnership();
        final List<StudentClassRubricCriteria> flatList = List.of(
                StudentClassRubricCriteria.builder()
                        .id(1).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID)
                        .rubricId(RUBRIC_ID).rubricTitle("Rubric A")
                        .studentName("Juan").studentSurnames("Garcia")
                        .criterionId(CRITERION_ID).criterionDescription("Good").qualification("Notable").gradeStart(5).gradeEnd(10)
                        .build(),
                StudentClassRubricCriteria.builder()
                        .id(2).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID)
                        .rubricId(RUBRIC_ID).rubricTitle("Rubric B")
                        .studentName("Juan").studentSurnames("Garcia")
                        .criterionId(51).criterionDescription("Bad").qualification("Insuficiente").gradeStart(0).gradeEnd(4)
                        .build());
        when(this.studentClassRubricCriteriaRepository.findByClassId(CLASS_ID)).thenReturn(flatList);

        final List<StudentCriteriaGroup> result = this.classRubricService.getAllStudentCriteriaByClassId(CLASS_ID);

        assertEquals(1, result.size());
        assertEquals(STUDENT_ID, result.get(0).getStudentId());
        assertEquals("Juan", result.get(0).getStudentName());
        assertEquals("Garcia", result.get(0).getStudentSurnames());
        assertEquals(2, result.get(0).getRubricCriteria().size());
        verify(this.studentClassRubricCriteriaRepository).findByClassId(CLASS_ID);
    }

    @Test
    void getStudentCriteriaByClassAndStudent_shouldReturnGroupedList_whenOwnershipIsValid() {
        this.setupSession();
        this.setupClassOwnership();
        when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).teacherId(TEACHER_ID).build()));
        when(this.studentClassRubricCriteriaRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                .thenReturn(List.of());

        final List<StudentCriteriaGroup> result =
                this.classRubricService.getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID);

        assertNotNull(result);
        verify(this.studentClassRubricCriteriaRepository).findByClassIdAndStudentId(CLASS_ID, STUDENT_ID);
    }

    @Test
    void assignCriterionToStudent_shouldSave_whenAllValidationsPass() {
        this.setupSession();
        this.setupClassRubricOwnership();
        when(this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
        when(this.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                .thenReturn(Optional.of(StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).deletionDate(null).build()));
        when(this.skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                .thenReturn(Optional.of(SkillRubricCriteria.builder().id(CRITERION_ID).rubricId(RUBRIC_ID).build()));
        when(this.studentClassRubricCriteriaRepository.existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(CLASS_RUBRIC_ID, STUDENT_ID))
                .thenReturn(false);
        final StudentClassRubricCriteria saved = StudentClassRubricCriteria.builder()
                .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build();
        when(this.studentClassRubricCriteriaRepository.save(any(StudentClassRubricCriteria.class))).thenReturn(saved);

        final StudentClassRubricCriteria result =
                this.classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);

        assertNotNull(result);
        assertEquals(CRITERIA_ID, result.getId());
        verify(this.studentClassRubricCriteriaRepository).save(any(StudentClassRubricCriteria.class));
    }

    @ParameterizedTest
    @MethodSource("assignCriterionToStudentValidationSetups")
    void assignCriterionToStudent_shouldThrowValidationException_whenValidationFails(
            Consumer<ClassRubricServiceImplTest> setup) {
        this.setupSession();
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
        setup.accept(this);

        assertThrows(StudentClassRubricCriteriaValidationException.class,
                () -> this.classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID));
    }

    static Stream<Consumer<ClassRubricServiceImplTest>> assignCriterionToStudentValidationSetups() {
        return Stream.of(
                t -> {
                    when(t.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                            .thenReturn(Optional.of(ClassRubric.builder()
                                    .id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build()));
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.empty());
                },
                t -> {
                    when(t.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                            .thenReturn(Optional.of(ClassRubric.builder()
                                    .id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build()));
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
                    when(t.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                            .thenReturn(Optional.empty());
                },
                t -> {
                    when(t.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                            .thenReturn(Optional.of(ClassRubric.builder()
                                    .id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build()));
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
                    when(t.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                            .thenReturn(Optional.of(StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).deletionDate(null).build()));
                    when(t.skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                            .thenReturn(Optional.empty());
                },
                t -> {
                    when(t.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                            .thenReturn(Optional.of(ClassRubric.builder()
                                    .id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build()));
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
                    when(t.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                            .thenReturn(Optional.of(StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).deletionDate(null).build()));
                    when(t.skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                            .thenReturn(Optional.of(SkillRubricCriteria.builder().id(CRITERION_ID).rubricId(999).build()));
                },
                t -> {
                    when(t.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                            .thenReturn(Optional.of(ClassRubric.builder()
                                    .id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build()));
                    when(t.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                            .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
                    when(t.studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                            .thenReturn(Optional.of(StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).deletionDate(null).build()));
                    when(t.skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                            .thenReturn(Optional.of(SkillRubricCriteria.builder().id(CRITERION_ID).rubricId(RUBRIC_ID).build()));
                    when(t.studentClassRubricCriteriaRepository.existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(CLASS_RUBRIC_ID, STUDENT_ID))
                            .thenReturn(true);
                }
        );
    }

    @Test
    void assignCriterionToStudent_shouldThrowClassRubricNotFoundException_whenClassRubricNotOwnedByTeacher() {
        this.setupSession();
        when(this.classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");

        assertThrows(ClassRubricNotFoundException.class,
                () -> this.classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID));
    }

    @Test
    void updateStudentCriterion_shouldUpdate_whenAllValidationsPass() {
        this.setupSession();
        when(this.studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                .thenReturn(Optional.of(StudentClassRubricCriteria.builder()
                        .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build()));
        this.setupClassRubricOwnership();
        when(this.skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                .thenReturn(Optional.of(SkillRubricCriteria.builder().id(CRITERION_ID).rubricId(RUBRIC_ID).build()));
        final StudentClassRubricCriteria updated = StudentClassRubricCriteria.builder()
                .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build();
        when(this.studentClassRubricCriteriaRepository.save(any(StudentClassRubricCriteria.class))).thenReturn(updated);

        final StudentClassRubricCriteria result = this.classRubricService.updateStudentCriterion(CRITERIA_ID, CRITERION_ID);

        assertNotNull(result);
        verify(this.studentClassRubricCriteriaRepository).save(any(StudentClassRubricCriteria.class));
    }

    @Test
    void updateStudentCriterion_shouldThrowStudentClassRubricCriteriaNotFoundException_whenNotOwnedByTeacher() {
        this.setupSession();
        when(this.studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");

        assertThrows(StudentClassRubricCriteriaNotFoundException.class,
                () -> this.classRubricService.updateStudentCriterion(CRITERIA_ID, CRITERION_ID));
    }

    @Test
    void removeStudentCriterion_shouldSoftDelete_whenCriterionBelongsToTeacher() {
        this.setupSession();
        when(this.studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                .thenReturn(Optional.of(StudentClassRubricCriteria.builder().id(CRITERIA_ID).build()));

        this.classRubricService.removeStudentCriterion(CRITERIA_ID);

        verify(this.studentClassRubricCriteriaRepository).softDeleteById(CRITERIA_ID);
    }

    @Test
    void removeStudentCriterion_shouldThrowStudentClassRubricCriteriaNotFoundException_whenNotOwnedByTeacher() {
        this.setupSession();
        when(this.studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");

        assertThrows(StudentClassRubricCriteriaNotFoundException.class,
                () -> this.classRubricService.removeStudentCriterion(CRITERIA_ID));
    }
}

