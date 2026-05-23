package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SubjectService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectUseCaseImplTest {

    private SubjectUseCaseImpl subjectUseCase;

    @Mock
    private SubjectService subjectService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    private static final Integer TEACHER_ID = 1;

    @BeforeEach
    void beforeEach() {
        subjectUseCase = new SubjectUseCaseImpl(subjectService, cascadeSoftDeleteService);
    }

    @Nested
    class GetSubjectsByTeacher {

        @Test
        void when_teacher_has_subjects_expect_list_returned() {
            final List<Subject> expected = List.of(
                    Subject.builder().id(1).name("Mathematics").teacherId(TEACHER_ID).build(),
                    Subject.builder().id(2).name("Science").teacherId(TEACHER_ID).build());
            when(subjectService.getSubjectsByTeacher()).thenReturn(expected);

            final List<Subject> result = subjectUseCase.getSubjectsByTeacher();

            assertThat(result).isNotNull().hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Mathematics");
            assertThat(result.get(1).getName()).isEqualTo("Science");
            verify(subjectService).getSubjectsByTeacher();
        }

        @Test
        void when_teacher_has_no_subjects_expect_empty_list() {
            when(subjectService.getSubjectsByTeacher()).thenReturn(List.of());

            final List<Subject> result = subjectUseCase.getSubjectsByTeacher();

            assertThat(result).isEmpty();
            verify(subjectService).getSubjectsByTeacher();
        }
    }

    @Nested
    class CreateSubject {

        @Test
        void when_creating_subject_expect_delegated_to_service() {
            final Subject subjectToCreate = Subject.builder().name("New Subject").build();
            final Subject createdSubject = Subject.builder().id(1).name("New Subject").teacherId(TEACHER_ID).build();
            when(subjectService.createSubject(any(Subject.class))).thenReturn(createdSubject);

            final Subject result = subjectUseCase.createSubject(subjectToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("New Subject");
            assertThat(result.getTeacherId()).isEqualTo(TEACHER_ID);
            verify(subjectService).createSubject(subjectToCreate);
        }
    }

    @Nested
    class SoftDeleteSubject {

        @Test
        void when_deleting_subject_expect_cascade_before_service() {
            final Integer subjectId = 1;
            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubject(subjectId);
            doNothing().when(subjectService).softDeleteSubject(subjectId);

            subjectUseCase.softDeleteSubject(subjectId);

            verify(cascadeSoftDeleteService).cascadeDeleteChildrenOfSubject(subjectId);
            verify(subjectService).softDeleteSubject(subjectId);
        }
    }

    @Nested
    class UpdateSubject {

        @Test
        void when_updating_subject_expect_delegated_to_service() {
            final Integer subjectId = 1;
            final Subject subjectToUpdate = Subject.builder().name("Updated Subject Name").build();
            final Subject updatedSubject = Subject.builder().id(subjectId).teacherId(TEACHER_ID).name("Updated Subject Name").build();
            when(subjectService.updateSubject(eq(subjectId), any(Subject.class))).thenReturn(updatedSubject);

            final Subject result = subjectUseCase.updateSubject(subjectId, subjectToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subjectId);
            assertThat(result.getName()).isEqualTo("Updated Subject Name");
            verify(subjectService).updateSubject(eq(subjectId), any(Subject.class));
        }

        @Test
        void when_updating_subject_expect_correct_data_passed_to_service() {
            final Integer subjectId = 1;
            final Subject subjectToUpdate = Subject.builder().name("Updated Name").build();
            when(subjectService.updateSubject(eq(subjectId), any(Subject.class)))
                    .thenAnswer(invocation -> {
                        final Subject s = invocation.getArgument(1);
                        return Subject.builder().id(subjectId).teacherId(TEACHER_ID).name(s.getName()).build();
                    });

            final Subject result = subjectUseCase.updateSubject(subjectId, subjectToUpdate);

            assertThat(result.getName()).isEqualTo("Updated Name");
            final ArgumentCaptor<Subject> subjectCaptor = ArgumentCaptor.forClass(Subject.class);
            verify(subjectService).updateSubject(eq(subjectId), subjectCaptor.capture());
            assertThat(subjectCaptor.getValue().getName()).isEqualTo("Updated Name");
        }
    }
}
