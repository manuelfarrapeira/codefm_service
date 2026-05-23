package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ScheduleNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ScheduleValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.ScheduleRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleServiceImplTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectClassRepository subjectClassRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    private ScheduleServiceImpl scheduleService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID = 20;
    private static final Integer SCHEDULE_ID = 100;

    @BeforeEach
    void beforeEach() {
        scheduleService = new ScheduleServiceImpl(
                scheduleRepository, classRepository, subjectRepository, subjectClassRepository, messageSource, sessionUser);

        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);

        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_CLASS_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Class not found.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_DAY_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("Day is required.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_DAY_INVALID), isNull(), any(Locale.class)))
                .thenReturn("Day must be between 1 and 5.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_ITEMS_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("At least one schedule item is required.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_SUBJECT_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Subject not found.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_START_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("Start time is required.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_END_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("End time is required.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_END_BEFORE_START), isNull(), any(Locale.class)))
                .thenReturn("End time must be after start time.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Schedule not found.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_IDS_REQUIRED), isNull(), any(Locale.class)))
                .thenReturn("At least one ID is required.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_IDS_NOT_OWNED), isNull(), any(Locale.class)))
                .thenReturn("Some schedules do not belong to the teacher.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_TIME_OVERLAP), isNull(), any(Locale.class)))
                .thenReturn("Schedule overlaps with an existing schedule for the same class and day.");
        when(messageSource.getMessage(eq(MessageKeys.SCHEDULE_VALIDATION_SUBJECT_NOT_IN_CLASS), any(), any(Locale.class)))
                .thenReturn("Subject is not assigned to this class.");
        when(messageSource.getMessage(eq(MessageKeys.CLASS_NOT_FOUND), isNull(), any(Locale.class)))
                .thenReturn("Class not found.");
        when(messageSource.getMessage(eq(MessageKeys.CLASS_FORBIDDEN), isNull(), any(Locale.class)))
                .thenReturn("Not authorized.");

        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
    }

    @Nested
    class GetSchedulesByClassId {

        @Test
        void when_class_belongs_to_teacher_expect_return_schedules() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> expectedSchedules = Arrays.asList(
                    Schedule.builder().id(1).classId(CLASS_ID).subjectId(SUBJECT_ID).day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build(),
                    Schedule.builder().id(2).classId(CLASS_ID).subjectId(SUBJECT_ID).day(2).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build()
            );

            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(scheduleRepository.findByClassId(CLASS_ID)).thenReturn(expectedSchedules);

            final List<Schedule> result = scheduleService.getSchedulesByClassId(CLASS_ID);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            verify(classRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
            verify(scheduleRepository, times(1)).findByClassId(CLASS_ID);
        }

        @Test
        void when_class_not_exists_expect_throw_not_found_exception() {
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> scheduleService.getSchedulesByClassId(CLASS_ID);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
            verify(scheduleRepository, never()).findByClassId(anyInt());
        }

        @Test
        void when_class_not_owned_by_teacher_expect_throw_forbidden_exception() {
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> scheduleService.getSchedulesByClassId(CLASS_ID);
            assertThatThrownBy(action).isInstanceOf(ClassForbiddenException.class);
            verify(scheduleRepository, never()).findByClassId(anyInt());
        }

        @Test
        void when_no_schedules_exist_expect_return_empty_list() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();

            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(scheduleRepository.findByClassId(CLASS_ID)).thenReturn(Collections.emptyList());

            final List<Schedule> result = scheduleService.getSchedulesByClassId(CLASS_ID);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class CreateSchedules {

        @Test
        void when_data_is_valid_expect_save_schedules() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );

            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
                final List<Schedule> schedules = invocation.getArgument(0);
                return schedules.stream().map(s -> Schedule.builder()
                        .id(1)
                        .classId(s.getClassId())
                        .subjectId(s.getSubjectId())
                        .day(s.getDay())
                        .start(s.getStart())
                        .end(s.getEnd())
                        .build()).toList();
            });

            final List<Schedule> result = scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(scheduleRepository, times(1)).saveAll(anyList());
        }

        @Test
        void when_class_not_exists_expect_throw_not_found_exception() {
            final List<Schedule> emptySchedules = new ArrayList<>();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, emptySchedules);
            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
            verify(scheduleRepository, never()).saveAll(anyList());
        }

        @Test
        void when_class_not_owned_by_teacher_expect_throw_forbidden_exception() {
            final List<Schedule> emptySchedules = new ArrayList<>();
            when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, emptySchedules);
            assertThatThrownBy(action).isInstanceOf(ClassForbiddenException.class);
            verify(scheduleRepository, never()).saveAll(anyList());
        }

        @Test
        void when_day_is_null_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, null, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("day"));
        }

        @Test
        void when_day_is_less_than_1_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 0, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("day"));
        }

        @Test
        void when_day_is_greater_than_5_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 6, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("day"));
        }

        @Test
        void when_schedules_list_is_null_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, null);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("items"));
        }

        @Test
        void when_schedules_list_is_empty_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> emptyList = new ArrayList<>();
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, emptyList);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("items"));
        }

        @Test
        void when_subject_not_found_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(999).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(999, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("subjectId"));
        }

        @Test
        void when_subject_id_is_null_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(null).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("subjectId"));
        }

        @Test
        void when_subject_not_assigned_to_class_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(false);

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("subjectId"));
            assertThat(ex.getErrors()).anyMatch(e -> e.getMessage().contains("not assigned to this class"));
        }

        @Test
        void when_start_time_is_null_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(null).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("start"));
        }

        @Test
        void when_end_time_is_null_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(null).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("end"));
        }

        @Test
        void when_end_time_is_before_start_time_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(10, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("end"));
        }

        @Test
        void when_end_time_equals_start_time_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(9, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("end"));
        }

        @Test
        void when_multiple_validations_fail_expect_collect_all_errors() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(null).start(null).end(null).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors().size()).isGreaterThanOrEqualTo(3);
        }

        @Test
        void when_all_data_valid_expect_save_multiple_schedules() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = Arrays.asList(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build(),
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build(),
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(12, 0)).end(LocalTime.of(13, 0)).build()
            );

            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
                final List<Schedule> schedules = invocation.getArgument(0);
                int id = 1;
                final List<Schedule> result = new ArrayList<>();
                for (final Schedule s : schedules) {
                    result.add(Schedule.builder()
                            .id(id++)
                            .classId(s.getClassId())
                            .subjectId(s.getSubjectId())
                            .day(s.getDay())
                            .start(s.getStart())
                            .end(s.getEnd())
                            .build());
                }
                return result;
            });

            final List<Schedule> result = scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            verify(scheduleRepository, times(1)).saveAll(anyList());
        }

        @Test
        void when_valid_expect_set_class_id_and_day_on_all_schedules() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = Arrays.asList(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );

            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
                final List<Schedule> schedules = invocation.getArgument(0);
                assertThat(schedules.get(0).getClassId()).isEqualTo(CLASS_ID);
                assertThat(schedules.get(0).getDay()).isEqualTo(3);
                return schedules;
            });

            scheduleService.createSchedules(CLASS_ID, 3, schedulesToCreate);

            verify(scheduleRepository, times(1)).saveAll(anyList());
        }

        @Test
        void when_time_overlaps_with_existing_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), any(LocalTime.class), any(LocalTime.class), isNull())).thenReturn(true);

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("time"));
            verify(scheduleRepository, never()).saveAll(anyList());
        }

        @Test
        void when_new_schedule_starts_during_existing_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), eq(LocalTime.of(8, 30)), eq(LocalTime.of(9, 30)), isNull())).thenReturn(true);

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("time"));
        }

        @Test
        void when_new_schedule_ends_after_existing_start_expect_throw_validation_exception() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(7, 30)).end(LocalTime.of(8, 30)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), eq(LocalTime.of(7, 30)), eq(LocalTime.of(8, 30)), isNull())).thenReturn(true);

            final ThrowingCallable action = () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().contains("time"));
        }

        @Test
        void when_no_time_overlap_expect_save_successfully() {
            final Class classEntity = Class.builder().id(CLASS_ID).build();
            final Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
            final List<Schedule> schedulesToCreate = List.of(
                    Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build()
            );
            when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
            when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
            when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
            when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), any(LocalTime.class), any(LocalTime.class), isNull())).thenReturn(false);
            when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            final List<Schedule> result = scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);

            assertThat(result).isNotNull();
            verify(scheduleRepository, times(1)).saveAll(anyList());
        }
    }

    @Nested
    class UpdateSchedule {

        @Test
        void when_data_is_valid_expect_update_schedule() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(2).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build();

            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
            when(scheduleRepository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            final Schedule result = scheduleService.updateSchedule(SCHEDULE_ID, updateData);

            assertThat(result).isNotNull();
            assertThat(result.getDay()).isEqualTo(2);
            assertThat(result.getStart()).isEqualTo(LocalTime.of(10, 0));
            assertThat(result.getEnd()).isEqualTo(LocalTime.of(11, 0));
            verify(scheduleRepository, times(1)).update(any(Schedule.class));
        }

        @Test
        void when_schedule_not_found_expect_throw_not_found_exception_with_message() {
            final Schedule updateData = Schedule.builder()
                    .day(2).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            assertThatThrownBy(action)
                    .isInstanceOf(ScheduleNotFoundException.class)
                    .hasMessage("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Schedule not found.]");
            verify(scheduleRepository, never()).update(any(Schedule.class));
        }

        @Test
        void when_day_is_null_expect_throw_validation_exception() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(null).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("day"));
        }

        @Test
        void when_day_is_invalid_expect_throw_validation_exception() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(7).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("day"));
        }

        @Test
        void when_start_time_is_null_expect_throw_validation_exception() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(2).start(null).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("start"));
        }

        @Test
        void when_end_time_is_null_expect_throw_validation_exception() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(2).start(LocalTime.of(10, 0)).end(null).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("end"));
        }

        @Test
        void when_end_time_is_before_start_time_expect_throw_validation_exception() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(2).start(LocalTime.of(12, 0)).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("end"));
        }

        @Test
        void when_time_overlaps_expect_throw_validation_exception() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(1).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
            when(scheduleRepository.existsOverlappingSchedule(CLASS_ID, 1, LocalTime.of(10, 0), LocalTime.of(11, 0), SCHEDULE_ID)).thenReturn(true);

            final ThrowingCallable action = () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("time"));
            verify(scheduleRepository, never()).update(any(Schedule.class));
        }

        @Test
        void when_no_time_overlap_expect_update_successfully() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(2).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
            when(scheduleRepository.existsOverlappingSchedule(CLASS_ID, 2, LocalTime.of(10, 0), LocalTime.of(11, 0), SCHEDULE_ID)).thenReturn(false);
            when(scheduleRepository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            final Schedule result = scheduleService.updateSchedule(SCHEDULE_ID, updateData);

            assertThat(result).isNotNull();
            assertThat(result.getDay()).isEqualTo(2);
            verify(scheduleRepository, times(1)).update(any(Schedule.class));
        }

        @Test
        void when_day_changes_to_different_day_expect_update_successfully() {
            final Schedule existingSchedule = Schedule.builder()
                    .id(SCHEDULE_ID).classId(CLASS_ID).subjectId(SUBJECT_ID)
                    .day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            final Schedule updateData = Schedule.builder()
                    .day(3).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build();
            when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
            when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(3), any(LocalTime.class), any(LocalTime.class), eq(SCHEDULE_ID))).thenReturn(false);
            when(scheduleRepository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            final Schedule result = scheduleService.updateSchedule(SCHEDULE_ID, updateData);

            assertThat(result).isNotNull();
            assertThat(result.getDay()).isEqualTo(3);
            verify(scheduleRepository, times(1)).update(any(Schedule.class));
        }
    }

    @Nested
    class SoftDeleteSchedules {

        @Test
        void when_all_belong_to_teacher_expect_delete_schedules() {
            final List<Integer> ids = Arrays.asList(1, 2, 3);

            when(scheduleRepository.allSchedulesBelongToTeacher(ids, TEACHER_ID)).thenReturn(true);

            scheduleService.softDeleteSchedules(ids);

            verify(scheduleRepository, times(1)).softDeleteSchedules(ids, TEACHER_ID);
        }

        @Test
        void when_ids_list_is_null_expect_throw_validation_exception() {
            final ThrowingCallable action = () -> scheduleService.softDeleteSchedules(null);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("ids"));
            verify(scheduleRepository, never()).softDeleteSchedules(anyList(), anyInt());
        }

        @Test
        void when_ids_list_is_empty_expect_throw_validation_exception() {
            final List<Integer> emptyList = new ArrayList<>();

            final ThrowingCallable action = () -> scheduleService.softDeleteSchedules(emptyList);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("ids"));
            verify(scheduleRepository, never()).softDeleteSchedules(anyList(), anyInt());
        }

        @Test
        void when_some_schedules_do_not_belong_to_teacher_expect_throw_validation_exception() {
            final List<Integer> ids = Arrays.asList(1, 2, 3);

            when(scheduleRepository.allSchedulesBelongToTeacher(ids, TEACHER_ID)).thenReturn(false);

            final ThrowingCallable action = () -> scheduleService.softDeleteSchedules(ids);
            final ScheduleValidationException ex = catchThrowableOfType(action, ScheduleValidationException.class);

            assertThat(ex.getErrors()).anyMatch(e -> e.getParam().equals("ids"));
            verify(scheduleRepository, never()).softDeleteSchedules(anyList(), anyInt());
        }
    }
}
