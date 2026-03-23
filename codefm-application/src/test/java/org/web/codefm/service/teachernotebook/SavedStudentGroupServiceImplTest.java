package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SavedStudentGroupValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SavedStudentGroupRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedStudentGroupServiceImplTest {

    private static final Integer CLASS_ID = 1;
    private static final Integer TEACHER_ID = 10;
    private static final Integer GROUP_ID = 100;

    @Mock
    private SavedStudentGroupRepository savedStudentGroupRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private SessionUser sessionUser;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private SavedStudentGroupServiceImpl savedStudentGroupService;

    @Test
    void getSavedGroupsByClassId_shouldReturnGroups_whenClassExists() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        final List<SavedStudentGroup> expected = List.of(
                SavedStudentGroup.builder().id(1).classId(CLASS_ID).name("Group A").build());
        when(this.savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID)).thenReturn(expected);

        final List<SavedStudentGroup> result = this.savedStudentGroupService.getSavedGroupsByClassId(CLASS_ID);

        assertEquals(expected, result);
        verify(this.savedStudentGroupRepository).findByClassIdWithMembers(CLASS_ID);
    }

    @Test
    void getSavedGroupsByClassId_shouldThrowClassNotFoundException_whenClassNotFound() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found.");

        assertThrows(ClassNotFoundException.class,
                () -> this.savedStudentGroupService.getSavedGroupsByClassId(CLASS_ID));
    }

    @Test
    void createSavedGroups_shouldThrowValidationException_whenClassAlreadyHasGroups() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2));
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_CLASS_ALREADY_HAS_GROUPS), any(), any(Locale.class)))
                .thenReturn("This class already has saved groups.");

        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                        .build());

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.createSavedGroups(CLASS_ID, groups));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("classId", ex.getErrors().get(0).getParam());
    }

    @Test
    void createSavedGroups_shouldCreateGroups_whenValid() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of());

        final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                .name("Group A")
                .members(List.of(
                        SavedStudentGroupMember.builder().studentId(1).build(),
                        SavedStudentGroupMember.builder().studentId(2).build(),
                        SavedStudentGroupMember.builder().studentId(3).build()))
                .build();
        final SavedStudentGroup savedGroup = SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).name("Group A").build();
        when(this.savedStudentGroupRepository.save(any())).thenReturn(savedGroup);

        final List<SavedStudentGroup> result = this.savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup));

        assertFalse(result.isEmpty());
        verify(this.savedStudentGroupRepository).save(any());
        verify(this.savedStudentGroupRepository).saveMembers(any());
    }

    @Test
    void createSavedGroups_shouldThrowValidationException_whenNameEmpty() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_NAME_REQUIRED), any(), any(Locale.class)))
                .thenReturn("Group name is required.");

        final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                .name("")
                .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                .build();

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup)));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("name", ex.getErrors().get(0).getParam());
    }

    @Test
    void createSavedGroups_shouldThrowValidationException_whenStudentsEmpty() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENTS_REQUIRED), any(), any(Locale.class)))
                .thenReturn("At least one student is required.");

        final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                .name("Group A")
                .members(List.of())
                .build();

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup)));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("studentIds", ex.getErrors().get(0).getParam());
    }

    @Test
    void createSavedGroups_shouldThrowValidationException_whenStudentNotInClass() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENT_NOT_IN_CLASS), any(), any(Locale.class)))
                .thenReturn("Student is not enrolled in this class: 999.");

        final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                .name("Group A")
                .members(List.of(SavedStudentGroupMember.builder().studentId(999).build()))
                .build();

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup)));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("studentIds", ex.getErrors().get(0).getParam());
    }

    @Test
    void updateAllSavedGroups_shouldUpdateGroups_whenValid() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99, 100));
        final SavedStudentGroup savedGroupA = SavedStudentGroup.builder().id(99).classId(CLASS_ID).name("Group A").build();
        final SavedStudentGroup savedGroupB = SavedStudentGroup.builder().id(100).classId(CLASS_ID).name("Group B").build();
        when(this.savedStudentGroupRepository.save(any()))
                .thenReturn(savedGroupA)
                .thenReturn(savedGroupB);

        final List<SavedStudentGroup> inputGroups = List.of(
                SavedStudentGroup.builder().id(99).name("Group A")
                        .members(List.of(
                                SavedStudentGroupMember.builder().studentId(1).build(),
                                SavedStudentGroupMember.builder().studentId(2).build()))
                        .build(),
                SavedStudentGroup.builder().id(100).name("Group B")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(3).build()))
                        .build());

        final List<SavedStudentGroup> result = this.savedStudentGroupService.updateAllSavedGroups(CLASS_ID, inputGroups);

        assertEquals(2, result.size());
        verify(this.savedStudentGroupRepository).hardDeleteMembersByGroupId(99);
        verify(this.savedStudentGroupRepository).hardDeleteMembersByGroupId(100);
        verify(this.savedStudentGroupRepository, times(2)).save(any());
        verify(this.savedStudentGroupRepository, times(2)).saveMembers(any());
    }

    @Test
    void updateAllSavedGroups_shouldThrowValidationException_whenGroupHasNoId() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99));
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_GROUP_ID_REQUIRED), any(), any(Locale.class)))
                .thenReturn("All groups must include their id.");

        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                        .build());

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("id", ex.getErrors().get(0).getParam());
    }

    @Test
    void updateAllSavedGroups_shouldThrowValidationException_whenGroupIdNotFound() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99));
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_GROUP_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Group with id 999 not found.");

        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().id(999).name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                        .build());

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("id", ex.getErrors().get(0).getParam());
    }

    @Test
    void updateAllSavedGroups_shouldThrowClassNotFoundException_whenClassNotFound() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found.");

        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                        .build());

        assertThrows(ClassNotFoundException.class,
                () -> this.savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups));
    }

    @Test
    void updateAllSavedGroups_shouldThrowValidationException_whenStudentDuplicated() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99, 100));
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENT_DUPLICATE), any(), any(Locale.class)))
                .thenReturn("Student appears in more than one group.");

        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().id(99).name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                        .build(),
                SavedStudentGroup.builder().id(100).name("Group B")
                        .members(List.of(
                                SavedStudentGroupMember.builder().studentId(1).build(),
                                SavedStudentGroupMember.builder().studentId(2).build()))
                        .build());

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("studentIds", ex.getErrors().get(0).getParam());
    }

    @Test
    void updateAllSavedGroups_shouldThrowValidationException_whenNotAllStudentsAssigned() {
        setupSessionMocks();
        setupClassAndStudentMocks();
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99));
        when(this.messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENTS_NOT_ALL_ASSIGNED), any(), any(Locale.class)))
                .thenReturn("Not all students are assigned to a group.");

        final List<SavedStudentGroup> groups = List.of(
                SavedStudentGroup.builder().id(99).name("Group A")
                        .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                        .build());

        final SavedStudentGroupValidationException ex = assertThrows(SavedStudentGroupValidationException.class,
                () -> this.savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups));

        assertFalse(ex.getErrors().isEmpty());
        assertEquals("studentIds", ex.getErrors().get(0).getParam());
    }


    @Test
    void softDeleteSavedGroupsByClassId_shouldDeleteAllGroups_whenClassExists() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2, 3));

        this.savedStudentGroupService.softDeleteSavedGroupsByClassId(CLASS_ID);

        verify(this.savedStudentGroupRepository).hardDeleteMembersByGroupIds(List.of(1, 2, 3));
        verify(this.savedStudentGroupRepository).softDeleteByClassId(CLASS_ID);
    }

    @Test
    void softDeleteSavedGroupsByClassId_shouldThrowClassNotFoundException_whenClassNotFound() {
        setupSessionMocks();
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                .thenReturn("Class not found.");

        assertThrows(ClassNotFoundException.class,
                () -> this.savedStudentGroupService.softDeleteSavedGroupsByClassId(CLASS_ID));
    }

    private void setupSessionMocks() {
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    private void setupClassAndStudentMocks() {
        when(this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
        when(this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2, 3));
        when(this.studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(any(), eq(TEACHER_ID)))
                .thenReturn(List.of(
                        Student.builder().id(1).build(),
                        Student.builder().id(2).build(),
                        Student.builder().id(3).build()));
    }
}
