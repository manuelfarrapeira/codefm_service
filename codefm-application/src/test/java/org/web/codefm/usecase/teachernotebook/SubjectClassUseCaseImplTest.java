package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SubjectClassService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectClassUseCaseImplTest {

    private SubjectClassUseCaseImpl subjectClassUseCase;

    @Mock
    private SubjectClassService subjectClassService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID_1 = 100;
    private static final Integer SUBJECT_ID_2 = 101;

    @BeforeEach
    void beforeEach() {
        subjectClassUseCase = new SubjectClassUseCaseImpl(subjectClassService, cascadeSoftDeleteService);
    }

    @Nested
    class GetSubjectsByClassId {

        @Test
        void when_subjects_found_expect_delegated_to_service() {
            final List<SubjectClassDetail> expected = List.of(
                    SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                    SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build());
            when(subjectClassService.getSubjectsByClassId(CLASS_ID)).thenReturn(expected);

            final List<SubjectClassDetail> result = subjectClassUseCase.getSubjectsByClassId(CLASS_ID);

            assertThat(result).isNotNull().hasSize(2);
            verify(subjectClassService).getSubjectsByClassId(CLASS_ID);
        }
    }

    @Nested
    class GetAllClassesWithSubjects {

        @Test
        void when_fetching_all_classes_expect_delegated_to_service() {
            final List<ClassWithSubjects> expected = List.of(
                    ClassWithSubjects.builder()
                            .classData(Class.builder().id(CLASS_ID).name("1A").build())
                            .subjects(List.of(SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build()))
                            .build());
            when(subjectClassService.getAllClassesWithSubjects()).thenReturn(expected);

            final List<ClassWithSubjects> result = subjectClassUseCase.getAllClassesWithSubjects();

            assertThat(result).isNotNull().hasSize(1);
            verify(subjectClassService).getAllClassesWithSubjects();
        }
    }

    @Nested
    class AssignSubjectsToClass {

        @Test
        void when_assigning_subjects_expect_delegated_to_service() {
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1, SUBJECT_ID_2);
            final List<SubjectClassDetail> expected = List.of(
                    SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                    SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build());
            when(subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds)).thenReturn(expected);

            final List<SubjectClassDetail> result = subjectClassUseCase.assignSubjectsToClass(CLASS_ID, subjectIds);

            assertThat(result).isNotNull().hasSize(2);
            verify(subjectClassService).assignSubjectsToClass(CLASS_ID, subjectIds);
        }
    }

    @Nested
    class RemoveSubjectsFromClass {

        @Test
        void when_removing_subjects_expect_cascade_before_service() {
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1, SUBJECT_ID_2);
            final List<Integer> subjectClassIds = List.of(200, 201);
            when(subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds)).thenReturn(subjectClassIds);
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubjectClass(anyInt());
            doNothing().when(subjectClassService).removeSubjectsFromClass(CLASS_ID, subjectIds);

            subjectClassUseCase.removeSubjectsFromClass(CLASS_ID, subjectIds);

            final var order = inOrder(cascadeSoftDeleteService, subjectClassService);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubjectClass(200);
            order.verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubjectClass(201);
            order.verify(subjectClassService).removeSubjectsFromClass(CLASS_ID, subjectIds);
        }

        @Test
        void when_removing_subjects_expect_find_active_ids_called_first() {
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1, SUBJECT_ID_2);
            final List<Integer> subjectClassIds = List.of(200, 201);
            when(subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds)).thenReturn(subjectClassIds);

            subjectClassUseCase.removeSubjectsFromClass(CLASS_ID, subjectIds);

            verify(subjectClassService).findActiveSubjectClassIds(CLASS_ID, subjectIds);
            verify(cascadeSoftDeleteService, times(2)).cascadeDeleteChildrenOfSubjectClass(anyInt());
            verify(subjectClassService).removeSubjectsFromClass(CLASS_ID, subjectIds);
        }
    }
}
