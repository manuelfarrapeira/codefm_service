package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SubjectClassService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectClassUseCaseImplTest {

    @Mock
    private SubjectClassService subjectClassService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @InjectMocks
    private SubjectClassUseCaseImpl subjectClassUseCase;

    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID_1 = 100;
    private static final Integer SUBJECT_ID_2 = 101;

    @Test
    void getSubjectsByClassId_shouldDelegateToService() {
        List<SubjectClassDetail> expectedSubjects = Arrays.asList(
                SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build()
        );

        when(subjectClassService.getSubjectsByClassId(CLASS_ID)).thenReturn(expectedSubjects);

        List<SubjectClassDetail> result = subjectClassUseCase.getSubjectsByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectClassService).getSubjectsByClassId(CLASS_ID);
    }

    @Test
    void getAllClassesWithSubjects_shouldDelegateToService() {
        List<ClassWithSubjects> expectedResult = Arrays.asList(
                ClassWithSubjects.builder()
                        .classData(Class.builder().id(CLASS_ID).name("1A").build())
                        .subjects(Arrays.asList(SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build()))
                        .build()
        );

        when(subjectClassService.getAllClassesWithSubjects()).thenReturn(expectedResult);

        List<ClassWithSubjects> result = subjectClassUseCase.getAllClassesWithSubjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(subjectClassService).getAllClassesWithSubjects();
    }

    @Test
    void assignSubjectsToClass_shouldDelegateToService() {
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
        List<SubjectClassDetail> expectedSubjects = Arrays.asList(
                SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build()
        );

        when(subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds)).thenReturn(expectedSubjects);

        List<SubjectClassDetail> result = subjectClassUseCase.assignSubjectsToClass(CLASS_ID, subjectIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subjectClassService).assignSubjectsToClass(CLASS_ID, subjectIds);
    }

    @Test
    void removeSubjectsFromClass_shouldCallCascadeBeforeService() {
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
        List<Integer> subjectClassIds = Arrays.asList(200, 201);

        when(subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds)).thenReturn(subjectClassIds);
        doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubjectClass(anyInt());
        doNothing().when(subjectClassService).removeSubjectsFromClass(CLASS_ID, subjectIds);

        subjectClassUseCase.removeSubjectsFromClass(CLASS_ID, subjectIds);

        var order = inOrder(cascadeSoftDeleteService, subjectClassService);
        order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubjectClass(200);
        order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubjectClass(201);
        order.verify(subjectClassService).removeSubjectsFromClass(CLASS_ID, subjectIds);
    }

    @Test
    void removeSubjectsFromClass_shouldCallFindActiveSubjectClassIdsFirst() {
        List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
        List<Integer> subjectClassIds = Arrays.asList(200, 201);

        when(subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds)).thenReturn(subjectClassIds);

        subjectClassUseCase.removeSubjectsFromClass(CLASS_ID, subjectIds);

        verify(subjectClassService, times(1)).findActiveSubjectClassIds(CLASS_ID, subjectIds);
        verify(cascadeSoftDeleteService, times(2)).cascadeDeleteChildrenOfSubjectClass(anyInt());
        verify(subjectClassService, times(1)).removeSubjectsFromClass(CLASS_ID, subjectIds);
    }
}
