package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ClassRubricService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassRubricUseCaseImplTest {

    private ClassRubricUseCaseImpl classRubricUseCase;

    @Mock
    private ClassRubricService classRubricService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer CLASS_ID = 10;
    private static final Integer RUBRIC_ID = 20;
    private static final Integer CLASS_RUBRIC_ID = 30;
    private static final Integer STUDENT_ID = 40;
    private static final Integer CRITERION_ID = 50;
    private static final Integer CRITERIA_ID = 70;

    @BeforeEach
    void beforeEach() {
        classRubricUseCase = new ClassRubricUseCaseImpl(classRubricService, cascadeSoftDeleteService);
    }

    @Nested
    class GetRubricsByClassId {

        @Test
        void when_rubrics_found_expect_delegated_to_service() {
            final List<ClassRubric> expected = List.of(
                    ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build());
            when(classRubricService.getRubricsByClassId(CLASS_ID)).thenReturn(expected);

            final List<ClassRubric> result = classRubricUseCase.getRubricsByClassId(CLASS_ID);

            assertThat(result).hasSize(1);
            verify(classRubricService).getRubricsByClassId(CLASS_ID);
        }
    }

    @Nested
    class AssignRubricToClass {

        @Test
        void when_assigning_rubric_expect_delegated_to_service() {
            final ClassRubric expected = ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build();
            when(classRubricService.assignRubricToClass(CLASS_ID, RUBRIC_ID)).thenReturn(expected);

            final ClassRubric result = classRubricUseCase.assignRubricToClass(CLASS_ID, RUBRIC_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CLASS_RUBRIC_ID);
            verify(classRubricService).assignRubricToClass(CLASS_ID, RUBRIC_ID);
        }
    }

    @Nested
    class RemoveRubricFromClass {

        @Test
        void when_removing_rubric_expect_cascade_before_service() {
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfClassRubric(CLASS_RUBRIC_ID);
            doNothing().when(classRubricService).removeRubricFromClass(CLASS_RUBRIC_ID);

            classRubricUseCase.removeRubricFromClass(CLASS_RUBRIC_ID);

            final var order = inOrder(cascadeSoftDeleteService, classRubricService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfClassRubric(CLASS_RUBRIC_ID);
            order.verify(classRubricService).removeRubricFromClass(CLASS_RUBRIC_ID);
        }
    }

    @Nested
    class GetAllStudentCriteriaByClassId {

        @Test
        void when_criteria_found_expect_delegated_to_service() {
            final List<StudentCriteriaGroup> expected = List.of(
                    StudentCriteriaGroup.builder().studentId(STUDENT_ID).studentName("Juan").studentSurnames("Garcia").rubricCriteria(List.of()).build());
            when(classRubricService.getAllStudentCriteriaByClassId(CLASS_ID)).thenReturn(expected);

            final List<StudentCriteriaGroup> result = classRubricUseCase.getAllStudentCriteriaByClassId(CLASS_ID);

            assertThat(result).hasSize(1);
            verify(classRubricService).getAllStudentCriteriaByClassId(CLASS_ID);
        }
    }

    @Nested
    class GetStudentCriteriaByClassAndStudent {

        @Test
        void when_criteria_requested_expect_delegated_to_service() {
            when(classRubricService.getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID)).thenReturn(List.of());

            final List<StudentCriteriaGroup> result = classRubricUseCase.getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID);

            assertThat(result).isNotNull();
            verify(classRubricService).getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID);
        }
    }

    @Nested
    class AssignCriterionToStudent {

        @Test
        void when_assigning_criterion_expect_delegated_to_service() {
            final StudentClassRubricCriteria expected = StudentClassRubricCriteria.builder()
                    .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build();
            when(classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID)).thenReturn(expected);

            final StudentClassRubricCriteria result = classRubricUseCase.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CRITERIA_ID);
            verify(classRubricService).assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);
        }
    }

    @Nested
    class UpdateStudentCriterion {

        @Test
        void when_updating_criterion_expect_delegated_to_service() {
            final StudentClassRubricCriteria expected = StudentClassRubricCriteria.builder()
                    .id(CRITERIA_ID).criterionId(CRITERION_ID).build();
            when(classRubricService.updateStudentCriterion(CRITERIA_ID, CRITERION_ID)).thenReturn(expected);

            final StudentClassRubricCriteria result = classRubricUseCase.updateStudentCriterion(CRITERIA_ID, CRITERION_ID);

            assertThat(result).isNotNull();
            verify(classRubricService).updateStudentCriterion(CRITERIA_ID, CRITERION_ID);
        }
    }

    @Nested
    class RemoveStudentCriterion {

        @Test
        void when_removing_criterion_expect_delegated_to_service() {
            doNothing().when(classRubricService).removeStudentCriterion(CRITERIA_ID);

            classRubricUseCase.removeStudentCriterion(CRITERIA_ID);

            verify(classRubricService).removeStudentCriterion(CRITERIA_ID);
        }
    }
}

