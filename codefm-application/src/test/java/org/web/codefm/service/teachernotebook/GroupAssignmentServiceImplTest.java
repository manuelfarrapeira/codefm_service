package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAssignmentServiceImplTest {

    private static final Integer CLASS_ID = 1;
    private static final Integer TEACHER_ID = 10;
    private static final Integer ASSIGNMENT_ID = 100;
    private static final Integer GROUP_ID = 50;

    @Mock
    private GroupAssignmentRepository groupAssignmentRepository;

    @Mock
    private GroupAssignmentGradeRepository groupAssignmentGradeRepository;

    @Mock
    private GroupAssignmentDocumentRepository groupAssignmentDocumentRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SavedStudentGroupRepository savedStudentGroupRepository;

    @Mock
    private SessionUser sessionUser;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private GroupAssignmentServiceImpl groupAssignmentService;

    @Test
    void getAssignmentsByClassId_shouldReturnAssignments_whenClassExists() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        final List<GroupAssignment> expected = List.of(
                GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").quarter(1).build());
        when(this.groupAssignmentRepository.findByClassId(CLASS_ID)).thenReturn(expected);
        when(this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(List.of(ASSIGNMENT_ID)))
                .thenReturn(Collections.emptyList());

        final List<GroupAssignment> result = this.groupAssignmentService.getAssignmentsByClassId(CLASS_ID);

        assertEquals(1, result.size());
        assertEquals(ASSIGNMENT_ID, result.get(0).getId());
        assertNotNull(result.get(0).getDocuments());
        verify(this.groupAssignmentDocumentRepository).findByGroupAssignmentIds(List.of(ASSIGNMENT_ID));
    }

    @Test
    void getAssignmentsByClassId_shouldThrowClassNotFoundException_whenClassNotFound() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found.");

        assertThrows(ClassNotFoundException.class,
                () -> this.groupAssignmentService.getAssignmentsByClassId(CLASS_ID));
    }

    @Test
    void createAssignment_shouldReturnCreated_whenValid() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(1).build();
        final GroupAssignment saved = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").quarter(1).build();
        when(this.groupAssignmentRepository.save(any(GroupAssignment.class))).thenReturn(saved);

        final GroupAssignment result = this.groupAssignmentService.createAssignment(CLASS_ID, input);

        assertEquals(saved, result);
        verify(this.groupAssignmentRepository).save(any(GroupAssignment.class));
    }

    @Test
    void createAssignment_shouldThrowValidationException_whenTitleIsEmpty() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_TITLE_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Title is required.");
        final GroupAssignment input = GroupAssignment.builder().title("").quarter(1).build();

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createAssignment(CLASS_ID, input));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("title", ex.getErrors().get(0).getParam());
    }

    @Test
    void createAssignment_shouldThrowValidationException_whenQuarterIsNull() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_QUARTER_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Quarter is required.");
        final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(null).build();

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createAssignment(CLASS_ID, input));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("quarter", ex.getErrors().get(0).getParam());
    }

    @ParameterizedTest
    @CsvSource({"0", "4", "-1", "10"})
    void createAssignment_shouldThrowValidationException_whenQuarterIsInvalid(int invalidQuarter) {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_QUARTER_INVALID), any(), any(Locale.class)))
                .thenReturn("Quarter must be 1, 2 or 3.");
        final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(invalidQuarter).build();

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createAssignment(CLASS_ID, input));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("quarter", ex.getErrors().get(0).getParam());
    }

    @Test
    void updateAssignment_shouldReturnUpdated_whenValid() {
        setupSessionMocks();
        final GroupAssignment existing = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Old Title").quarter(1).build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(existing));
        final GroupAssignment input = GroupAssignment.builder().title("New Title").description("Desc").quarter(2).build();
        when(this.groupAssignmentRepository.save(any(GroupAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        final GroupAssignment result = this.groupAssignmentService.updateAssignment(ASSIGNMENT_ID, input);

        assertEquals("New Title", result.getTitle());
        assertEquals("Desc", result.getDescription());
        assertEquals(2, result.getQuarter());
    }

    @Test
    void updateAssignment_shouldThrowNotFoundException_whenAssignmentNotFound() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Group assignment not found.");
        final GroupAssignment input = GroupAssignment.builder().title("Title").quarter(1).build();

        assertThrows(GroupAssignmentNotFoundException.class,
                () -> this.groupAssignmentService.updateAssignment(ASSIGNMENT_ID, input));
    }

    @Test
    void softDeleteAssignment_shouldCallRepository_whenAssignmentExists() {
        setupSessionMocks();
        final GroupAssignment existing = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(existing));

        this.groupAssignmentService.softDeleteAssignment(ASSIGNMENT_ID);

        verify(this.groupAssignmentRepository).softDeleteById(ASSIGNMENT_ID);
    }

    @Test
    void softDeleteAssignment_shouldThrowNotFoundException_whenAssignmentNotFound() {
        setupSessionMocks();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Group assignment not found.");

        assertThrows(GroupAssignmentNotFoundException.class,
                () -> this.groupAssignmentService.softDeleteAssignment(ASSIGNMENT_ID));
    }

    @Test
    void getGradesByAssignmentId_shouldReturnGrades_whenAssignmentExists() {
        setupSessionMocks();
        final GroupAssignment existing = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(existing));
        final List<GroupAssignmentGrade> expected = List.of(
                GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(8.5).build());
        when(this.groupAssignmentGradeRepository.findByAssignmentId(ASSIGNMENT_ID)).thenReturn(expected);
        when(this.groupAssignmentDocumentRepository.findByAssignmentId(ASSIGNMENT_ID))
                .thenReturn(Collections.emptyList());

        final List<GroupAssignmentGrade> result = this.groupAssignmentService.getGradesByAssignmentId(ASSIGNMENT_ID);

        assertEquals(1, result.size());
        assertEquals(8.5, result.get(0).getGrade());
        assertNotNull(result.get(0).getDocuments());
        verify(this.groupAssignmentDocumentRepository).findByAssignmentId(ASSIGNMENT_ID);
    }

    @Test
    void createOrUpdateGrade_shouldCreateNewGrade_whenNoExistingGrade() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).build()));
        when(this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                .thenReturn(Optional.empty());
        final GroupAssignmentGrade saved = GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(8.5).build();
        when(this.groupAssignmentGradeRepository.save(any(GroupAssignmentGrade.class))).thenReturn(saved);

        final GroupAssignmentGrade result = this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 8.5);

        assertEquals(saved, result);
        verify(this.groupAssignmentGradeRepository).save(any(GroupAssignmentGrade.class));
    }

    @Test
    void createOrUpdateGrade_shouldUpdateExistingGrade_whenGradeExists() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).build()));
        final GroupAssignmentGrade existingGrade = GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(5.0).build();
        when(this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                .thenReturn(Optional.of(existingGrade));
        when(this.groupAssignmentGradeRepository.update(any(GroupAssignmentGrade.class))).thenAnswer(inv -> inv.getArgument(0));

        final GroupAssignmentGrade result = this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0);

        assertEquals(9.0, result.getGrade());
        verify(this.groupAssignmentGradeRepository).update(any(GroupAssignmentGrade.class));
        verify(this.groupAssignmentGradeRepository, never()).save(any(GroupAssignmentGrade.class));
    }

    @Test
    void createOrUpdateGrade_shouldThrowValidationException_whenGradeIsNull() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GRADE_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Grade is required.");

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, null));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("grade", ex.getErrors().get(0).getParam());
    }

    @ParameterizedTest
    @CsvSource({"-0.1", "10.1", "-5.0", "15.0"})
    void createOrUpdateGrade_shouldThrowValidationException_whenGradeIsOutOfRange(double invalidGrade) {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GRADE_INVALID), any(), any(Locale.class)))
                .thenReturn("Grade must be between 0 and 10.");

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, invalidGrade));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("grade", ex.getErrors().get(0).getParam());
    }

    @Test
    void createOrUpdateGrade_shouldThrowValidationException_whenGroupNotFound() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Group not found.");

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 8.0));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("groupId", ex.getErrors().get(0).getParam());
    }

    @Test
    void createOrUpdateGrade_shouldThrowValidationException_whenGroupNotInClass() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(999).build()));
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS), any(), any(Locale.class)))
                .thenReturn("Group not in class.");

        final GroupAssignmentValidationException ex = assertThrows(GroupAssignmentValidationException.class,
                () -> this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 8.0));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("groupId", ex.getErrors().get(0).getParam());
    }

    @Test
    void deleteGrade_shouldCallRepository_whenGradeExists() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        final GroupAssignmentGrade existingGrade = GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(8.0).build();
        when(this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                .thenReturn(Optional.of(existingGrade));

        this.groupAssignmentService.deleteGrade(ASSIGNMENT_ID, GROUP_ID);

        verify(this.groupAssignmentGradeRepository).softDeleteById(1);
    }

    @Test
    void deleteGrade_shouldThrowNotFoundException_whenGradeNotFound() {
        setupSessionMocks();
        final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
        when(this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                .thenReturn(Optional.of(assignment));
        when(this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Not found.");

        assertThrows(GroupAssignmentNotFoundException.class,
                () -> this.groupAssignmentService.deleteGrade(ASSIGNMENT_ID, GROUP_ID));
    }

    private void setupSessionMocks() {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }
}

