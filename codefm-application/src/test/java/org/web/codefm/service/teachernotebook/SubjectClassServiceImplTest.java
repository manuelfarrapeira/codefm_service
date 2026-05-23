package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SubjectClassValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectClassServiceImplTest {

    @Mock
    private SubjectClassRepository subjectClassRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SessionUser sessionUser;

    private SubjectClassServiceImpl subjectClassService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID_1 = 100;
    private static final Integer SUBJECT_ID_2 = 101;

    @BeforeEach
    void beforeEach() {
        this.subjectClassService = new SubjectClassServiceImpl(this.subjectClassRepository, this.classRepository,
                this.subjectRepository, this.messageSource, this.sessionUser);
    }

    @Nested
    class GetSubjectsByClassId {

        @Test
        void when_class_belongs_to_teacher_expect_subjects() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final List<SubjectClassDetail> expectedSubjects = Arrays.asList(
                    SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                    SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build()
            );

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));
            when(subjectClassRepository.findSubjectsByClassId(CLASS_ID)).thenReturn(expectedSubjects);

            final List<SubjectClassDetail> result = subjectClassService.getSubjectsByClassId(CLASS_ID);

            assertThat(result).isNotNull().hasSize(2);
            verify(classRepository).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
            verify(subjectClassRepository).findSubjectsByClassId(CLASS_ID);
        }

        @Test
        void when_class_does_not_exist_expect_not_found_exception() {
            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found");

            final ThrowingCallable callable = () -> subjectClassService.getSubjectsByClassId(CLASS_ID);

            assertThatThrownBy(callable).isInstanceOf(ClassNotFoundException.class);
            verify(subjectClassRepository, never()).findSubjectsByClassId(any());
        }

        @Test
        void when_class_does_not_belong_to_teacher_expect_class_forbidden_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_FORBIDDEN), any(), any(Locale.class)))
                    .thenReturn("Forbidden");

            final ThrowingCallable callable = () -> subjectClassService.getSubjectsByClassId(CLASS_ID);

            assertThatThrownBy(callable).isInstanceOf(ClassForbiddenException.class);
            verify(subjectClassRepository, never()).findSubjectsByClassId(any());
        }
    }

    @Nested
    class GetAllClassesWithSubjects {

        @Test
        void when_teacher_has_classes_expect_all_classes_with_subjects() {
            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            final List<ClassWithSubjects> expectedResult = List.of(
                    ClassWithSubjects.builder()
                            .classData(Class.builder().id(CLASS_ID).name("1A").build())
                            .subjects(List.of(SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build()))
                            .build()
            );

            when(subjectClassRepository.findAllClassesWithSubjectsByTeacherId(TEACHER_ID)).thenReturn(expectedResult);

            final List<ClassWithSubjects> result = subjectClassService.getAllClassesWithSubjects();

            assertThat(result).isNotNull().hasSize(1);
            verify(subjectClassRepository).findAllClassesWithSubjectsByTeacherId(TEACHER_ID);
        }
    }

    @Nested
    class AssignSubjectsToClass {

        @Test
        void when_all_validations_pass_expect_subjects_to_be_assigned() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
            final List<SubjectClassDetail> expectedSubjects = Arrays.asList(
                    SubjectClassDetail.builder().subjectClassId(200).subjectId(SUBJECT_ID_1).subjectName("Math").build(),
                    SubjectClassDetail.builder().subjectClassId(201).subjectId(SUBJECT_ID_2).subjectName("Science").build()
            );

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_1, TEACHER_ID))
                    .thenReturn(Optional.of(Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build()));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_2, TEACHER_ID))
                    .thenReturn(Optional.of(Subject.builder().id(SUBJECT_ID_2).name("Science").teacherId(TEACHER_ID).build()));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(anyInt(), eq(CLASS_ID)))
                    .thenReturn(false);
            when(subjectClassRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(subjectClassRepository.findSubjectsByClassId(CLASS_ID)).thenReturn(expectedSubjects);

            final List<SubjectClassDetail> result = subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds);

            assertThat(result).isNotNull().hasSize(2);
            verify(subjectClassRepository).saveAll(anyList());
        }

        @Test
        void when_subject_ids_are_empty_expect_validation_exception() {
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Subject IDs required");

            final ThrowingCallable callable = () -> subjectClassService.assignSubjectsToClass(CLASS_ID, new ArrayList<>());

            assertThatThrownBy(callable).isInstanceOf(SubjectClassValidationException.class);
            verify(subjectClassRepository, never()).saveAll(any());
        }

        @Test
        void when_subject_ids_are_null_expect_validation_exception() {
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Subject IDs required");

            final ThrowingCallable callable = () -> subjectClassService.assignSubjectsToClass(CLASS_ID, null);

            assertThatThrownBy(callable).isInstanceOf(SubjectClassValidationException.class);
            verify(subjectClassRepository, never()).saveAll(any());
        }

        @Test
        void when_subject_is_not_owned_by_teacher_expect_validation_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1);

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_1, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_NOT_OWNED), any(), any(Locale.class)))
                    .thenReturn("Subject not owned");

            final ThrowingCallable callable = () -> subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds);

            assertThatThrownBy(callable).isInstanceOf(SubjectClassValidationException.class);
            verify(subjectClassRepository, never()).saveAll(any());
        }

        @Test
        void when_subject_is_already_assigned_expect_validation_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final Subject subject = Subject.builder().id(SUBJECT_ID_1).name("Math").teacherId(TEACHER_ID).build();
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1);

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID_1, TEACHER_ID))
                    .thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(true);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_ALREADY_EXISTS), any(), any(Locale.class)))
                    .thenReturn("Already exists");

            final ThrowingCallable callable = () -> subjectClassService.assignSubjectsToClass(CLASS_ID, subjectIds);

            assertThatThrownBy(callable).isInstanceOf(SubjectClassValidationException.class);
            verify(subjectClassRepository, never()).saveAll(any());
        }
    }

    @Nested
    class RemoveSubjectsFromClass {

        @Test
        void when_subjects_are_removed_expect_soft_delete_call() {
            final List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);
            doNothing().when(subjectClassRepository).softDeleteAll(CLASS_ID, subjectIds);

            assertThatNoException().isThrownBy(() -> subjectClassService.removeSubjectsFromClass(CLASS_ID, subjectIds));

            verify(subjectClassRepository).softDeleteAll(CLASS_ID, subjectIds);
        }
    }

    @Nested
    class FindActiveSubjectClassIds {

        @Test
        void when_class_does_not_exist_expect_not_found_exception() {
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1);

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Class not found");

            final ThrowingCallable callable = () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds);

            assertThatThrownBy(callable).isInstanceOf(ClassNotFoundException.class);
            verify(subjectClassRepository, never()).softDeleteAll(anyInt(), anyList());
        }

        @Test
        void when_class_does_not_belong_to_teacher_expect_class_forbidden_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1);

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(messageSource.getMessage(eq(MessageKeys.CLASS_FORBIDDEN), any(), any(Locale.class)))
                    .thenReturn("Forbidden");

            final ThrowingCallable callable = () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds);

            assertThatThrownBy(callable).isInstanceOf(ClassForbiddenException.class);
            verify(subjectClassRepository, never()).softDeleteAll(any(), any());
        }

        @Test
        void when_subject_ids_are_empty_expect_validation_exception() {
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_SUBJECT_IDS_REQUIRED), any(), any(Locale.class)))
                    .thenReturn("Subject IDs required");

            final ThrowingCallable callable = () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, new ArrayList<>());

            assertThatThrownBy(callable).isInstanceOf(SubjectClassValidationException.class);
            verify(subjectClassRepository, never()).softDeleteAll(any(), any());
        }

        @Test
        void when_association_does_not_exist_expect_validation_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final List<Integer> subjectIds = List.of(SUBJECT_ID_1);

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(false);
            when(messageSource.getMessage(eq(MessageKeys.SUBJECT_CLASS_NOT_FOUND), any(), any(Locale.class)))
                    .thenReturn("Subject not assigned to class");

            final ThrowingCallable callable = () -> subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds);

            assertThatThrownBy(callable).isInstanceOf(SubjectClassValidationException.class);
            verify(subjectClassRepository, never()).softDeleteAll(any(), any());
        }

        @Test
        void when_associations_exist_expect_subject_class_ids() {
            final Class clazz = Class.builder().id(CLASS_ID).schoolId(1).name("1A").build();
            final List<Integer> subjectIds = Arrays.asList(SUBJECT_ID_1, SUBJECT_ID_2);

            lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
            lenient().when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(clazz));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(true);
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID_2, CLASS_ID))
                    .thenReturn(true);
            when(subjectClassRepository.findIdBySubjectIdAndClassId(SUBJECT_ID_1, CLASS_ID))
                    .thenReturn(Optional.of(200));
            when(subjectClassRepository.findIdBySubjectIdAndClassId(SUBJECT_ID_2, CLASS_ID))
                    .thenReturn(Optional.of(201));

            final List<Integer> result = subjectClassService.findActiveSubjectClassIds(CLASS_ID, subjectIds);

            assertThat(result).isNotNull().hasSize(2).contains(200, 201);
        }
    }
}

