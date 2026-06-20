package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import static org.assertj.core.api.Assertions.*;
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

    private GroupAssignmentServiceImpl groupAssignmentService;

    @BeforeEach
    void beforeEach() {
        this.groupAssignmentService = new GroupAssignmentServiceImpl(
                this.groupAssignmentRepository,
                this.groupAssignmentGradeRepository,
                this.groupAssignmentDocumentRepository,
                this.classRepository,
                this.savedStudentGroupRepository,
                this.sessionUser,
                this.messageSource
        );
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
    }

    @Nested
    class GetAssignmentsByClassId {

        @Test
        void when_class_exists_expect_return_assignments() {
            when(GroupAssignmentServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            final List<GroupAssignment> expected = List.of(
                    GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").quarter(1).build());
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByClassId(CLASS_ID)).thenReturn(expected);
            when(GroupAssignmentServiceImplTest.this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(List.of(ASSIGNMENT_ID)))
                    .thenReturn(Collections.emptyList());

            final List<GroupAssignment> result = GroupAssignmentServiceImplTest.this.groupAssignmentService.getAssignmentsByClassId(CLASS_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(ASSIGNMENT_ID);
            assertThat(result.get(0).getDocuments()).isNotNull();
            verify(GroupAssignmentServiceImplTest.this.groupAssignmentDocumentRepository).findByGroupAssignmentIds(List.of(ASSIGNMENT_ID));
        }

        @Test
        void when_class_is_not_found_expect_throw_class_not_found_exception() {
            when(GroupAssignmentServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found.");

            final ThrowingCallable action = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.getAssignmentsByClassId(CLASS_ID);

            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }
    }

    @Nested
    class CreateAssignment {

        @Test
        void when_input_is_valid_expect_return_created_assignment() {
            when(GroupAssignmentServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(1).build();
            final GroupAssignment saved = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Project 1").quarter(1).build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.save(any(GroupAssignment.class))).thenReturn(saved);

            final GroupAssignment result = GroupAssignmentServiceImplTest.this.groupAssignmentService.createAssignment(CLASS_ID, input);

            assertThat(result).isEqualTo(saved);
            verify(GroupAssignmentServiceImplTest.this.groupAssignmentRepository).save(any(GroupAssignment.class));
        }

        @Test
        void when_title_is_empty_expect_throw_validation_exception() {
            when(GroupAssignmentServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_TITLE_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Title is required.");
            final GroupAssignment input = GroupAssignment.builder().title("").quarter(1).build();

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createAssignment(CLASS_ID, input);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("title");
        }

        @Test
        void when_quarter_is_null_expect_throw_validation_exception() {
            when(GroupAssignmentServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_QUARTER_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Quarter is required.");
            final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(null).build();

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createAssignment(CLASS_ID, input);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("quarter");
        }

        @ParameterizedTest
        @CsvSource({"0", "4", "-1", "10"})
        void when_quarter_is_invalid_expect_throw_validation_exception(final int invalidQuarter) {
            when(GroupAssignmentServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(org.web.codefm.domain.entity.teachernotebook.Class.builder().id(CLASS_ID).build()));
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_QUARTER_INVALID), any(), any(Locale.class)))
                    .thenReturn("Quarter must be 1, 2 or 3.");
            final GroupAssignment input = GroupAssignment.builder().title("Project 1").quarter(invalidQuarter).build();

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createAssignment(CLASS_ID, input);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("quarter");
        }
    }

    @Nested
    class UpdateAssignment {

        @Test
        void when_input_is_valid_expect_return_updated_assignment() {
            final GroupAssignment existing = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Old Title").quarter(1).build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(existing));
            final GroupAssignment input = GroupAssignment.builder().title("New Title").description("Desc").quarter(2).build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.save(any(GroupAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

            final GroupAssignment result = GroupAssignmentServiceImplTest.this.groupAssignmentService.updateAssignment(ASSIGNMENT_ID, input);

            assertThat(result.getTitle()).isEqualTo("New Title");
            assertThat(result.getDescription()).isEqualTo("Desc");
            assertThat(result.getQuarter()).isEqualTo(2);
        }

        @Test
        void when_assignment_is_not_found_expect_throw_not_found_exception() {
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Group assignment not found.");
            final GroupAssignment input = GroupAssignment.builder().title("Title").quarter(1).build();

            final ThrowingCallable action = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.updateAssignment(ASSIGNMENT_ID, input);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentNotFoundException.class);
        }
    }

    @Nested
    class SoftDeleteAssignment {

        @Test
        void when_assignment_exists_expect_call_repository() {
            final GroupAssignment existing = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(existing));

            GroupAssignmentServiceImplTest.this.groupAssignmentService.softDeleteAssignment(ASSIGNMENT_ID);

            verify(GroupAssignmentServiceImplTest.this.groupAssignmentRepository).softDeleteById(ASSIGNMENT_ID);
        }

        @Test
        void when_assignment_is_not_found_expect_throw_not_found_exception() {
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Group assignment not found.");

            final ThrowingCallable action = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.softDeleteAssignment(ASSIGNMENT_ID);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentNotFoundException.class);
        }
    }

    @Nested
    class GetGradesByAssignmentId {

        @Test
        void when_assignment_exists_expect_return_grades() {
            final GroupAssignment existing = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(existing));
            final List<GroupAssignmentGrade> expected = List.of(
                    GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(8.5).build());
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.findByAssignmentId(ASSIGNMENT_ID)).thenReturn(expected);
            when(GroupAssignmentServiceImplTest.this.groupAssignmentDocumentRepository.findByAssignmentId(ASSIGNMENT_ID))
                    .thenReturn(Collections.emptyList());

            final List<GroupAssignmentGrade> result = GroupAssignmentServiceImplTest.this.groupAssignmentService.getGradesByAssignmentId(ASSIGNMENT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGrade()).isEqualTo(8.5);
            assertThat(result.get(0).getDocuments()).isNotNull();
            verify(GroupAssignmentServiceImplTest.this.groupAssignmentDocumentRepository).findByAssignmentId(ASSIGNMENT_ID);
        }
    }

    @Nested
    class CreateOrUpdateGrade {

        @Test
        void when_no_existing_grade_expect_create_new_grade() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).build()));
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                    .thenReturn(Optional.empty());
            final GroupAssignmentGrade saved = GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(8.5).build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.save(any(GroupAssignmentGrade.class))).thenReturn(saved);

            final GroupAssignmentGrade result = GroupAssignmentServiceImplTest.this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 8.5);

            assertThat(result).isEqualTo(saved);
            verify(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository).save(any(GroupAssignmentGrade.class));
        }

        @Test
        void when_grade_exists_expect_update_existing_grade() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(CLASS_ID).build()));
            final GroupAssignmentGrade existingGrade = GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(5.0).build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                    .thenReturn(Optional.of(existingGrade));
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.update(any(GroupAssignmentGrade.class))).thenAnswer(inv -> inv.getArgument(0));

            final GroupAssignmentGrade result = GroupAssignmentServiceImplTest.this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 9.0);

            assertThat(result.getGrade()).isEqualTo(9.0);
            verify(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository).update(any(GroupAssignmentGrade.class));
            verify(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository, never()).save(any(GroupAssignmentGrade.class));
        }

        @Test
        void when_grade_is_null_expect_throw_validation_exception() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GRADE_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Grade is required.");

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, null);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("grade");
        }

        @ParameterizedTest
        @CsvSource({"-0.1", "10.1", "-5.0", "15.0"})
        void when_grade_is_out_of_range_expect_throw_validation_exception(final double invalidGrade) {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GRADE_INVALID), any(), any(Locale.class)))
                    .thenReturn("Grade must be between 0 and 10.");

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, invalidGrade);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("grade");
        }

        @Test
        void when_group_is_not_found_expect_throw_validation_exception() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Group not found.");

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 8.0);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("groupId");
        }

        @Test
        void when_group_is_not_in_class_expect_throw_validation_exception() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.savedStudentGroupRepository.findByIdAndTeacherId(GROUP_ID, TEACHER_ID))
                    .thenReturn(Optional.of(SavedStudentGroup.builder().id(GROUP_ID).classId(999).build()));
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS), any(), any(Locale.class)))
                    .thenReturn("Group not in class.");

            final ThrowingCallable call = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.createOrUpdateGrade(ASSIGNMENT_ID, GROUP_ID, 8.0);
            final Throwable throwable = catchThrowable(call);

            assertThat(throwable).isInstanceOf(GroupAssignmentValidationException.class);
            final GroupAssignmentValidationException ex = (GroupAssignmentValidationException) throwable;
            assertThat(ex.getErrors()).isNotEmpty();
            assertThat(ex.getErrors().get(0).getParam()).isEqualTo("groupId");
        }
    }

    @Nested
    class DeleteGrade {

        @Test
        void when_grade_exists_expect_call_repository() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            final GroupAssignmentGrade existingGrade = GroupAssignmentGrade.builder().id(1).groupAssignmentId(ASSIGNMENT_ID).groupId(GROUP_ID).grade(8.0).build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                    .thenReturn(Optional.of(existingGrade));

            GroupAssignmentServiceImplTest.this.groupAssignmentService.deleteGrade(ASSIGNMENT_ID, GROUP_ID);

            verify(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository).softDeleteById(1);
        }

        @Test
        void when_grade_is_not_found_expect_throw_not_found_exception() {
            final GroupAssignment assignment = GroupAssignment.builder().id(ASSIGNMENT_ID).classId(CLASS_ID).title("Title").build();
            when(GroupAssignmentServiceImplTest.this.groupAssignmentRepository.findByIdAndTeacherId(ASSIGNMENT_ID, TEACHER_ID))
                    .thenReturn(Optional.of(assignment));
            when(GroupAssignmentServiceImplTest.this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(ASSIGNMENT_ID, GROUP_ID))
                    .thenReturn(Optional.empty());
            when(GroupAssignmentServiceImplTest.this.messageSource.getMessage(eq(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Not found.");

            final ThrowingCallable action = () -> GroupAssignmentServiceImplTest.this.groupAssignmentService.deleteGrade(ASSIGNMENT_ID, GROUP_ID);

            assertThatThrownBy(action).isInstanceOf(GroupAssignmentNotFoundException.class);
        }
    }
}

