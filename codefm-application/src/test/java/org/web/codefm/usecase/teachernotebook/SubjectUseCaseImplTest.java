package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.service.teachernotebook.SubjectService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectUseCaseImplTest {

    @Mock
    private SubjectService subjectService;

    @InjectMocks
    private SubjectUseCaseImpl subjectUseCase;

    private static final Integer TEACHER_ID = 1;

    @Test
    void getSubjectsByTeacher_shouldReturnSubjects_whenTeacherHasSubjects() {
        List<Subject> expectedSubjects = Arrays.asList(
                Subject.builder().id(1).name("Mathematics").teacherId(TEACHER_ID).build(),
                Subject.builder().id(2).name("Science").teacherId(TEACHER_ID).build()
        );

        when(subjectService.getSubjectsByTeacher()).thenReturn(expectedSubjects);

        List<Subject> result = subjectUseCase.getSubjectsByTeacher();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Mathematics", result.get(0).getName());
        assertEquals("Science", result.get(1).getName());
        verify(subjectService, times(1)).getSubjectsByTeacher();
    }

    @Test
    void getSubjectsByTeacher_shouldReturnEmptyList_whenTeacherHasNoSubjects() {
        when(subjectService.getSubjectsByTeacher()).thenReturn(Collections.emptyList());

        List<Subject> result = subjectUseCase.getSubjectsByTeacher();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subjectService, times(1)).getSubjectsByTeacher();
    }

    @Test
    void createSubject_shouldCallServiceWithSubject() {
        Subject subjectToCreate = Subject.builder()
                .name("New Subject")
                .build();
        Subject createdSubject = Subject.builder()
                .id(1)
                .name("New Subject")
                .teacherId(TEACHER_ID)
                .build();

        when(subjectService.createSubject(any(Subject.class))).thenReturn(createdSubject);

        Subject result = subjectUseCase.createSubject(subjectToCreate);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Subject", result.getName());
        assertEquals(TEACHER_ID, result.getTeacherId());
        verify(subjectService, times(1)).createSubject(subjectToCreate);
    }

    @Test
    void softDeleteSubject_shouldCallServiceWithSubjectId() {
        Integer subjectId = 1;

        doNothing().when(subjectService).softDeleteSubject(subjectId);

        subjectUseCase.softDeleteSubject(subjectId);

        verify(subjectService, times(1)).softDeleteSubject(subjectId);
    }

    @Test
    void updateSubject_shouldCallServiceWithSubjectIdAndSubject() {
        Integer subjectId = 1;

        Subject subjectToUpdate = Subject.builder()
                .name("Updated Subject Name")
                .build();
        Subject updatedSubject = Subject.builder()
                .id(subjectId)
                .teacherId(TEACHER_ID)
                .name("Updated Subject Name")
                .build();

        when(subjectService.updateSubject(eq(subjectId), any(Subject.class)))
                .thenReturn(updatedSubject);

        Subject result = subjectUseCase.updateSubject(subjectId, subjectToUpdate);

        assertNotNull(result);
        assertEquals(subjectId, result.getId());
        assertEquals("Updated Subject Name", result.getName());
        verify(subjectService, times(1)).updateSubject(eq(subjectId), any(Subject.class));
    }

    @Test
    void updateSubject_shouldPassCorrectSubjectDataToService() {
        Integer subjectId = 1;
        Subject subjectToUpdate = Subject.builder()
                .name("Updated Name")
                .build();

        when(subjectService.updateSubject(eq(subjectId), any(Subject.class)))
                .thenAnswer(invocation -> {
                    Subject s = invocation.getArgument(1);
                    return Subject.builder()
                            .id(subjectId)
                            .teacherId(TEACHER_ID)
                            .name(s.getName())
                            .build();
                });

        Subject result = subjectUseCase.updateSubject(subjectId, subjectToUpdate);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());

        ArgumentCaptor<Subject> subjectCaptor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectService).updateSubject(eq(subjectId), subjectCaptor.capture());
        assertEquals("Updated Name", subjectCaptor.getValue().getName());
    }
}
