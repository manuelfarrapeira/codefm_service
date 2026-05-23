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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private ClassRubricServiceImpl classRubricService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer RUBRIC_ID = 20;
    private static final Integer CLASS_RUBRIC_ID = 30;
    private static final Integer STUDENT_ID = 40;
    private static final Integer CRITERION_ID = 50;
    private static final Integer SKILL_ID = 60;
    private static final Integer CRITERIA_ID = 70;

    @BeforeEach
    void beforeEach() {
        this.classRubricService = new ClassRubricServiceImpl(this.classRubricRepository,
                this.studentClassRubricCriteriaRepository, this.classRepository, this.skillRubricRepository,
                this.skillRepository, this.studentRepository, this.studentClassRepository,
                this.skillRubricCriteriaRepository, this.messageSource, this.sessionUser);
    }

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

    @Nested
    class GetRubricsByClassId {

        @Test
        void when_class_belongs_to_teacher_expect_rubrics() {
            setupSession();
            setupClassOwnership();
            final List<ClassRubric> expected = List.of(
                    ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build());
            when(classRubricRepository.findByClassId(CLASS_ID)).thenReturn(expected);

            final List<ClassRubric> result = classRubricService.getRubricsByClassId(CLASS_ID);

            assertThat(result).isNotNull().hasSize(1);
            verify(classRubricRepository).findByClassId(CLASS_ID);
        }

        @Test
        void when_class_is_not_owned_by_teacher_expect_not_found_exception() {
            setupSession();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            final ThrowingCallable callable = () -> classRubricService.getRubricsByClassId(CLASS_ID);

            assertThatThrownBy(callable).isInstanceOf(ClassRubricNotFoundException.class);
        }
    }

    @Nested
    class AssignRubricToClass {

        @Test
        void when_all_validations_pass_expect_assignment_to_be_saved() {
            setupSession();
            setupClassOwnership();
            when(skillRubricRepository.findById(RUBRIC_ID))
                    .thenReturn(Optional.of(SkillRubric.builder().id(RUBRIC_ID).skillId(SKILL_ID).build()));
            when(skillRepository.findByIdAndTeacherId(SKILL_ID, TEACHER_ID))
                    .thenReturn(Optional.of(Skill.builder().id(SKILL_ID).teacherId(TEACHER_ID).build()));
            when(classRubricRepository.existsByClassIdAndRubricIdAndDeletionDateIsNull(CLASS_ID, RUBRIC_ID))
                    .thenReturn(false);
            final ClassRubric saved = ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build();
            when(classRubricRepository.save(any(ClassRubric.class))).thenReturn(saved);

            final ClassRubric result = classRubricService.assignRubricToClass(CLASS_ID, RUBRIC_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CLASS_RUBRIC_ID);
            verify(classRubricRepository).save(any(ClassRubric.class));
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.ClassRubricServiceImplTest#assignRubricToClassValidationSetups")
        void when_validation_fails_expect_class_rubric_validation_exception(Consumer<ClassRubricServiceImplTest> setup) {
            setupSession();
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            setup.accept(ClassRubricServiceImplTest.this);
            final ThrowingCallable callable = () -> classRubricService.assignRubricToClass(CLASS_ID, RUBRIC_ID);

            assertThatThrownBy(callable).isInstanceOf(ClassRubricValidationException.class);
        }
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

    @Nested
    class RemoveRubricFromClass {

        @Test
        void when_class_rubric_belongs_to_teacher_expect_soft_delete() {
            setupSession();
            setupClassRubricOwnership();

            classRubricService.removeRubricFromClass(CLASS_RUBRIC_ID);

            verify(classRubricRepository).softDeleteById(CLASS_RUBRIC_ID);
        }

        @Test
        void when_class_rubric_is_not_owned_by_teacher_expect_not_found_exception() {
            setupSession();
            when(classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            final ThrowingCallable callable = () -> classRubricService.removeRubricFromClass(CLASS_RUBRIC_ID);

            assertThatThrownBy(callable).isInstanceOf(ClassRubricNotFoundException.class);
        }
    }

    @Nested
    class GetAllStudentCriteriaByClassId {

        @Test
        void when_class_belongs_to_teacher_expect_grouped_list() {
            setupSession();
            setupClassOwnership();
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
            when(studentClassRubricCriteriaRepository.findByClassId(CLASS_ID)).thenReturn(flatList);

            final List<StudentCriteriaGroup> result = classRubricService.getAllStudentCriteriaByClassId(CLASS_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(result.get(0).getStudentName()).isEqualTo("Juan");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("Garcia");
            assertThat(result.get(0).getRubricCriteria()).hasSize(2);
            verify(studentClassRubricCriteriaRepository).findByClassId(CLASS_ID);
        }
    }

    @Nested
    class GetStudentCriteriaByClassAndStudent {

        @Test
        void when_ownership_is_valid_expect_grouped_list() {
            setupSession();
            setupClassOwnership();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).teacherId(TEACHER_ID).build()));
            when(studentClassRubricCriteriaRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                    .thenReturn(List.of());

            final List<StudentCriteriaGroup> result = classRubricService.getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID);

            assertThat(result).isNotNull();
            verify(studentClassRubricCriteriaRepository).findByClassIdAndStudentId(CLASS_ID, STUDENT_ID);
        }
    }

    @Nested
    class AssignCriterionToStudent {

        @Test
        void when_all_validations_pass_expect_student_criterion_to_be_saved() {
            setupSession();
            setupClassRubricOwnership();
            when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(STUDENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(Student.builder().id(STUDENT_ID).build()));
            when(studentClassRepository.findByClassIdAndStudentId(CLASS_ID, STUDENT_ID))
                    .thenReturn(Optional.of(StudentClass.builder().classId(CLASS_ID).studentId(STUDENT_ID).deletionDate(null).build()));
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                    .thenReturn(Optional.of(SkillRubricCriteria.builder().id(CRITERION_ID).rubricId(RUBRIC_ID).build()));
            when(studentClassRubricCriteriaRepository.existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(CLASS_RUBRIC_ID, STUDENT_ID))
                    .thenReturn(false);
            final StudentClassRubricCriteria saved = StudentClassRubricCriteria.builder()
                    .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build();
            when(studentClassRubricCriteriaRepository.save(any(StudentClassRubricCriteria.class))).thenReturn(saved);

            final StudentClassRubricCriteria result = classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CRITERIA_ID);
            verify(studentClassRubricCriteriaRepository).save(any(StudentClassRubricCriteria.class));
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.service.teachernotebook.ClassRubricServiceImplTest#assignCriterionToStudentValidationSetups")
        void when_validation_fails_expect_student_class_rubric_criteria_validation_exception(Consumer<ClassRubricServiceImplTest> setup) {
            setupSession();
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            setup.accept(ClassRubricServiceImplTest.this);
            final ThrowingCallable callable = () -> classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);

            assertThatThrownBy(callable).isInstanceOf(StudentClassRubricCriteriaValidationException.class);
        }

        @Test
        void when_class_rubric_is_not_owned_by_teacher_expect_class_rubric_not_found_exception() {
            setupSession();
            when(classRubricRepository.findByIdAndTeacherId(CLASS_RUBRIC_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            final ThrowingCallable callable = () -> classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);

            assertThatThrownBy(callable).isInstanceOf(ClassRubricNotFoundException.class);
        }
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

    @Nested
    class UpdateStudentCriterion {

        @Test
        void when_all_validations_pass_expect_student_criterion_to_be_updated() {
            setupSession();
            when(studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                    .thenReturn(Optional.of(StudentClassRubricCriteria.builder()
                            .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build()));
            setupClassRubricOwnership();
            when(skillRubricCriteriaRepository.findActiveById(CRITERION_ID))
                    .thenReturn(Optional.of(SkillRubricCriteria.builder().id(CRITERION_ID).rubricId(RUBRIC_ID).build()));
            final StudentClassRubricCriteria updated = StudentClassRubricCriteria.builder()
                    .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build();
            when(studentClassRubricCriteriaRepository.save(any(StudentClassRubricCriteria.class))).thenReturn(updated);

            final StudentClassRubricCriteria result = classRubricService.updateStudentCriterion(CRITERIA_ID, CRITERION_ID);

            assertThat(result).isNotNull();
            verify(studentClassRubricCriteriaRepository).save(any(StudentClassRubricCriteria.class));
        }

        @Test
        void when_not_owned_by_teacher_expect_student_class_rubric_criteria_not_found_exception() {
            setupSession();
            when(studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            final ThrowingCallable callable = () -> classRubricService.updateStudentCriterion(CRITERIA_ID, CRITERION_ID);

            assertThatThrownBy(callable).isInstanceOf(StudentClassRubricCriteriaNotFoundException.class);
        }
    }

    @Nested
    class RemoveStudentCriterion {

        @Test
        void when_criterion_belongs_to_teacher_expect_soft_delete() {
            setupSession();
            when(studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                    .thenReturn(Optional.of(StudentClassRubricCriteria.builder().id(CRITERIA_ID).build()));

            classRubricService.removeStudentCriterion(CRITERIA_ID);

            verify(studentClassRubricCriteriaRepository).softDeleteById(CRITERIA_ID);
        }

        @Test
        void when_not_owned_by_teacher_expect_student_class_rubric_criteria_not_found_exception() {
            setupSession();
            when(studentClassRubricCriteriaRepository.findByIdAndTeacherId(CRITERIA_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("error");
            final ThrowingCallable callable = () -> classRubricService.removeStudentCriterion(CRITERIA_ID);

            assertThatThrownBy(callable).isInstanceOf(StudentClassRubricCriteriaNotFoundException.class);
        }
    }
}

