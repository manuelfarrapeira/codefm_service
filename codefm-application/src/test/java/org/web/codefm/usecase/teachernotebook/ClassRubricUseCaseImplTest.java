package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.ClassRubric;
import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;
import org.web.codefm.domain.entity.teachernotebook.StudentCriteriaGroup;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ClassRubricService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassRubricUseCaseImplTest {

    @Mock
    private ClassRubricService classRubricService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @InjectMocks
    private ClassRubricUseCaseImpl classRubricUseCase;

    private static final Integer CLASS_ID = 10;
    private static final Integer RUBRIC_ID = 20;
    private static final Integer CLASS_RUBRIC_ID = 30;
    private static final Integer STUDENT_ID = 40;
    private static final Integer CRITERION_ID = 50;
    private static final Integer CRITERIA_ID = 70;

    @Test
    void getRubricsByClassId_shouldDelegateToService() {
        final List<ClassRubric> expected = List.of(
                ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build());
        when(this.classRubricService.getRubricsByClassId(CLASS_ID)).thenReturn(expected);

        final List<ClassRubric> result = this.classRubricUseCase.getRubricsByClassId(CLASS_ID);

        assertEquals(1, result.size());
        verify(this.classRubricService).getRubricsByClassId(CLASS_ID);
    }

    @Test
    void assignRubricToClass_shouldDelegateToService() {
        final ClassRubric expected = ClassRubric.builder().id(CLASS_RUBRIC_ID).classId(CLASS_ID).rubricId(RUBRIC_ID).build();
        when(this.classRubricService.assignRubricToClass(CLASS_ID, RUBRIC_ID)).thenReturn(expected);

        final ClassRubric result = this.classRubricUseCase.assignRubricToClass(CLASS_ID, RUBRIC_ID);

        assertNotNull(result);
        assertEquals(CLASS_RUBRIC_ID, result.getId());
        verify(this.classRubricService).assignRubricToClass(CLASS_ID, RUBRIC_ID);
    }

    @Test
    void removeRubricFromClass_shouldCallCascadeBeforeService() {
        doNothing().when(this.cascadeSoftDeleteService).cascadeDeleteChildrenOfClassRubric(CLASS_RUBRIC_ID);
        doNothing().when(this.classRubricService).removeRubricFromClass(CLASS_RUBRIC_ID);

        this.classRubricUseCase.removeRubricFromClass(CLASS_RUBRIC_ID);

        final var order = inOrder(this.cascadeSoftDeleteService, this.classRubricService);
        order.verify(this.cascadeSoftDeleteService).cascadeDeleteChildrenOfClassRubric(CLASS_RUBRIC_ID);
        order.verify(this.classRubricService).removeRubricFromClass(CLASS_RUBRIC_ID);
    }

    @Test
    void getAllStudentCriteriaByClassId_shouldDelegateToService() {
        final List<StudentCriteriaGroup> expected = List.of(
                StudentCriteriaGroup.builder().studentId(STUDENT_ID).studentName("Juan").studentSurnames("Garcia").rubricCriteria(List.of()).build());
        when(this.classRubricService.getAllStudentCriteriaByClassId(CLASS_ID)).thenReturn(expected);

        final List<StudentCriteriaGroup> result = this.classRubricUseCase.getAllStudentCriteriaByClassId(CLASS_ID);

        assertEquals(1, result.size());
        verify(this.classRubricService).getAllStudentCriteriaByClassId(CLASS_ID);
    }

    @Test
    void getStudentCriteriaByClassAndStudent_shouldDelegateToService() {
        when(this.classRubricService.getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID))
                .thenReturn(List.of());

        final List<StudentCriteriaGroup> result =
                this.classRubricUseCase.getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID);

        assertNotNull(result);
        verify(this.classRubricService).getStudentCriteriaByClassAndStudent(CLASS_ID, STUDENT_ID);
    }

    @Test
    void assignCriterionToStudent_shouldDelegateToService() {
        final StudentClassRubricCriteria expected = StudentClassRubricCriteria.builder()
                .id(CRITERIA_ID).classRubricId(CLASS_RUBRIC_ID).studentId(STUDENT_ID).criterionId(CRITERION_ID).build();
        when(this.classRubricService.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID))
                .thenReturn(expected);

        final StudentClassRubricCriteria result =
                this.classRubricUseCase.assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);

        assertNotNull(result);
        assertEquals(CRITERIA_ID, result.getId());
        verify(this.classRubricService).assignCriterionToStudent(CLASS_RUBRIC_ID, STUDENT_ID, CRITERION_ID);
    }

    @Test
    void updateStudentCriterion_shouldDelegateToService() {
        final StudentClassRubricCriteria expected = StudentClassRubricCriteria.builder()
                .id(CRITERIA_ID).criterionId(CRITERION_ID).build();
        when(this.classRubricService.updateStudentCriterion(CRITERIA_ID, CRITERION_ID)).thenReturn(expected);

        final StudentClassRubricCriteria result = this.classRubricUseCase.updateStudentCriterion(CRITERIA_ID, CRITERION_ID);

        assertNotNull(result);
        verify(this.classRubricService).updateStudentCriterion(CRITERIA_ID, CRITERION_ID);
    }

    @Test
    void removeStudentCriterion_shouldDelegateToService() {
        doNothing().when(this.classRubricService).removeStudentCriterion(CRITERIA_ID);

        this.classRubricUseCase.removeStudentCriterion(CRITERIA_ID);

        verify(this.classRubricService).removeStudentCriterion(CRITERIA_ID);
    }
}

