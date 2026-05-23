package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private SavedStudentGroupServiceImpl savedStudentGroupService;

    @BeforeEach
    void beforeEach() {
        savedStudentGroupService = new SavedStudentGroupServiceImpl(
                savedStudentGroupRepository, classRepository, studentRepository,
                studentClassRepository, sessionUser, messageSource);
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

    @Nested
    class GetSavedGroupsByClassId {

        @Test
        void when_class_exists_expect_return_groups() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            final List<SavedStudentGroup> expected = List.of(
                    SavedStudentGroup.builder().id(1).classId(CLASS_ID).name("Group A").build());
            when(savedStudentGroupRepository.findByClassIdWithMembers(CLASS_ID)).thenReturn(expected);

            final List<SavedStudentGroup> result = savedStudentGroupService.getSavedGroupsByClassId(CLASS_ID);

            assertThat(result).isEqualTo(expected);
            verify(savedStudentGroupRepository).findByClassIdWithMembers(CLASS_ID);
        }

        @Test
        void when_class_not_found_expect_throw_class_not_found_exception() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found.");

            final ThrowingCallable action = () -> savedStudentGroupService.getSavedGroupsByClassId(CLASS_ID);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }
    }

    @Nested
    class CreateSavedGroups {

        @Test
        void when_class_already_has_groups_expect_throw_validation_exception() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2));
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_CLASS_ALREADY_HAS_GROUPS), any(), any(Locale.class)))
                    .thenReturn("This class already has saved groups.");

            final List<SavedStudentGroup> groups = List.of(
                    SavedStudentGroup.builder().name("Group A")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                            .build());

            final ThrowingCallable action = () -> savedStudentGroupService.createSavedGroups(CLASS_ID, groups);
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("classId");
        }

        @Test
        void when_valid_expect_create_groups() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of());

            final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                    .name("Group A")
                    .members(List.of(
                            SavedStudentGroupMember.builder().studentId(1).build(),
                            SavedStudentGroupMember.builder().studentId(2).build(),
                            SavedStudentGroupMember.builder().studentId(3).build()))
                    .build();
            final SavedStudentGroup savedGroup = SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).name("Group A").build();
            when(savedStudentGroupRepository.saveAll(any())).thenReturn(List.of(savedGroup));

            final List<SavedStudentGroup> result = savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup));

            assertThat(result).isNotEmpty();
            verify(savedStudentGroupRepository).saveAll(any());
            verify(savedStudentGroupRepository).saveMembers(any());
        }

        @Test
        void when_name_is_empty_expect_throw_validation_exception() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_NAME_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Group name is required.");

            final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                    .name("")
                    .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                    .build();

            final ThrowingCallable action = () -> savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup));
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("name");
        }

        @Test
        void when_students_list_is_empty_expect_throw_validation_exception() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENTS_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("At least one student is required.");

            final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                    .name("Group A")
                    .members(List.of())
                    .build();

            final ThrowingCallable action = () -> savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup));
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("studentIds");
        }

        @Test
        void when_student_not_in_class_expect_throw_validation_exception() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENT_NOT_IN_CLASS), any(), any(Locale.class)))
                    .thenReturn("Student is not enrolled in this class: 999.");

            final SavedStudentGroup inputGroup = SavedStudentGroup.builder()
                    .name("Group A")
                    .members(List.of(SavedStudentGroupMember.builder().studentId(999).build()))
                    .build();

            final ThrowingCallable action = () -> savedStudentGroupService.createSavedGroups(CLASS_ID, List.of(inputGroup));
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("studentIds");
        }
    }

    @Nested
    class UpdateAllSavedGroups {

        @Test
        void when_valid_expect_update_groups() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99, 100));

            final List<SavedStudentGroup> inputGroups = List.of(
                    SavedStudentGroup.builder().id(99).name("Group A")
                            .members(List.of(
                                    SavedStudentGroupMember.builder().studentId(1).build(),
                                    SavedStudentGroupMember.builder().studentId(2).build()))
                            .build(),
                    SavedStudentGroup.builder().id(100).name("Group B")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(3).build()))
                            .build());

            final List<SavedStudentGroup> result = savedStudentGroupService.updateAllSavedGroups(CLASS_ID, inputGroups);

            assertThat(result).hasSize(2);
            verify(savedStudentGroupRepository).updateName(99, "Group A");
            verify(savedStudentGroupRepository).updateName(100, "Group B");
            verify(savedStudentGroupRepository).hardDeleteMembersByGroupIds(List.of(99, 100));
            verify(savedStudentGroupRepository).saveMembers(any());
        }

        @Test
        void when_group_has_no_id_expect_throw_validation_exception() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99));
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_GROUP_ID_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("All groups must include their id.");

            final List<SavedStudentGroup> groups = List.of(
                    SavedStudentGroup.builder().name("Group A")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                            .build());

            final ThrowingCallable action = () -> savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups);
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("id");
        }

        @Test
        void when_group_id_not_found_expect_throw_validation_exception() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99));
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_GROUP_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Group with id 999 not found.");

            final List<SavedStudentGroup> groups = List.of(
                    SavedStudentGroup.builder().id(999).name("Group A")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                            .build());

            final ThrowingCallable action = () -> savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups);
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("id");
        }

        @Test
        void when_class_not_found_expect_throw_class_not_found_exception() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found.");

            final List<SavedStudentGroup> groups = List.of(
                    SavedStudentGroup.builder().name("Group A")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                            .build());

            final ThrowingCallable action = () -> savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        void when_student_is_duplicated_across_groups_expect_throw_validation_exception() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99, 100));
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENT_DUPLICATE), any(), any(Locale.class)))
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

            final ThrowingCallable action = () -> savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups);
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("studentIds");
        }

        @Test
        void when_not_all_students_assigned_expect_throw_validation_exception() {
            setupSessionMocks();
            setupClassAndStudentMocks();
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(99));
            when(messageSource.getMessage(eq(MessageKeys.SAVED_GROUP_VALIDATION_STUDENTS_NOT_ALL_ASSIGNED), any(), any(Locale.class)))
                    .thenReturn("Not all students are assigned to a group.");

            final List<SavedStudentGroup> groups = List.of(
                    SavedStudentGroup.builder().id(99).name("Group A")
                            .members(List.of(SavedStudentGroupMember.builder().studentId(1).build()))
                            .build());

            final ThrowingCallable action = () -> savedStudentGroupService.updateAllSavedGroups(CLASS_ID, groups);
            final SavedStudentGroupValidationException ex = catchThrowableOfType(action, SavedStudentGroupValidationException.class);

            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("studentIds");
        }
    }

    @Nested
    class SoftDeleteSavedGroupsByClassId {

        @Test
        void when_class_exists_expect_delete_all_groups() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(savedStudentGroupRepository.findActiveIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2, 3));

            savedStudentGroupService.softDeleteSavedGroupsByClassId(CLASS_ID);

            verify(savedStudentGroupRepository).hardDeleteMembersByGroupIds(List.of(1, 2, 3));
            verify(savedStudentGroupRepository).softDeleteByClassId(CLASS_ID);
        }

        @Test
        void when_class_not_found_expect_throw_class_not_found_exception() {
            setupSessionMocks();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found.");

            final ThrowingCallable action = () -> savedStudentGroupService.softDeleteSavedGroupsByClassId(CLASS_ID);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }
    }
}
