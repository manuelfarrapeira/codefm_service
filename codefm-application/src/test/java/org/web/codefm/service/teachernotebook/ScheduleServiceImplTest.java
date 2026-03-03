package org.web.codefm.service.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 10;
    private static final Integer SUBJECT_ID = 20;
    private static final Integer SCHEDULE_ID = 100;

    @BeforeEach
    void setUp() {
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

    @Test
    void getSchedulesByClassId_shouldReturnSchedules_whenClassBelongsToTeacher() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> expectedSchedules = Arrays.asList(
                Schedule.builder().id(1).classId(CLASS_ID).subjectId(SUBJECT_ID).day(1).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build(),
                Schedule.builder().id(2).classId(CLASS_ID).subjectId(SUBJECT_ID).day(2).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build()
        );

        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(scheduleRepository.findByClassId(CLASS_ID)).thenReturn(expectedSchedules);

        List<Schedule> result = scheduleService.getSchedulesByClassId(CLASS_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(classRepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID);
        verify(scheduleRepository, times(1)).findByClassId(CLASS_ID);
    }

    @Test
    void getSchedulesByClassId_shouldThrowNotFoundException_whenClassNotExists() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ClassNotFoundException.class,
                () -> scheduleService.getSchedulesByClassId(CLASS_ID));
        verify(scheduleRepository, never()).findByClassId(anyInt());
    }

    @Test
    void getSchedulesByClassId_shouldThrowForbiddenException_whenClassNotOwnedByTeacher() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ClassForbiddenException.class,
                () -> scheduleService.getSchedulesByClassId(CLASS_ID));
        verify(scheduleRepository, never()).findByClassId(anyInt());
    }

    @Test
    void getSchedulesByClassId_shouldReturnEmptyList_whenNoSchedulesExist() {
        Class classEntity = Class.builder().id(CLASS_ID).build();

        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(scheduleRepository.findByClassId(CLASS_ID)).thenReturn(Collections.emptyList());

        List<Schedule> result = scheduleService.getSchedulesByClassId(CLASS_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createSchedules_shouldSaveSchedules_whenDataIsValid() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );

        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Schedule> schedules = invocation.getArgument(0);
            return schedules.stream().map(s -> Schedule.builder()
                    .id(1)
                    .classId(s.getClassId())
                    .subjectId(s.getSubjectId())
                    .day(s.getDay())
                    .start(s.getStart())
                    .end(s.getEnd())
                    .build()).toList();
        });

        List<Schedule> result = scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createSchedules_shouldThrowNotFoundException_whenClassNotExists() {
        List<Schedule> emptySchedules = new ArrayList<>();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ClassNotFoundException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, emptySchedules));
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void createSchedules_shouldThrowForbiddenException_whenClassNotOwnedByTeacher() {
        List<Schedule> emptySchedules = new ArrayList<>();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ClassForbiddenException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, emptySchedules));
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void createSchedules_shouldThrowException_whenDayIsNull() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, null, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("day")));
    }

    @Test
    void createSchedules_shouldThrowException_whenDayIsLessThan1() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 0, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("day")));
    }

    @Test
    void createSchedules_shouldThrowException_whenDayIsGreaterThan5() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 6, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("day")));
    }

    @Test
    void createSchedules_shouldThrowException_whenSchedulesListIsNull() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, null));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("items")));
    }

    @Test
    void createSchedules_shouldThrowException_whenSchedulesListIsEmpty() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> emptyList = new ArrayList<>();
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, emptyList));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("items")));
    }

    @Test
    void createSchedules_shouldThrowException_whenSubjectNotFound() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(999).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(999, TEACHER_ID)).thenReturn(Optional.empty());

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("subjectId")));
    }

    @Test
    void createSchedules_shouldThrowException_whenSubjectIdIsNull() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(null).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("subjectId")));
    }

    @Test
    void createSchedules_shouldThrowException_whenSubjectNotAssignedToClass() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(false);

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("subjectId")));
        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getMessage().contains("not assigned to this class")));
    }

    @Test
    void createSchedules_shouldThrowException_whenStartTimeIsNull() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(null).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("start")));
    }

    @Test
    void createSchedules_shouldThrowException_whenEndTimeIsNull() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(null).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("end")));
    }

    @Test
    void createSchedules_shouldThrowException_whenEndTimeIsBeforeStartTime() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(10, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("end")));
    }

    @Test
    void createSchedules_shouldThrowException_whenEndTimeEqualsStartTime() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(9, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("end")));
    }

    @Test
    void createSchedules_shouldCollectMultipleErrors_whenMultipleValidationsFail() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(null).start(null).end(null).build()
        );

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().size() >= 3);
    }

    @Test
    void updateSchedule_shouldUpdateSchedule_whenDataIsValid() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();

        Schedule updateData = Schedule.builder()
                .day(2)
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();

        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule result = scheduleService.updateSchedule(SCHEDULE_ID, updateData);

        assertNotNull(result);
        assertEquals(2, result.getDay());
        assertEquals(LocalTime.of(10, 0), result.getStart());
        assertEquals(LocalTime.of(11, 0), result.getEnd());
        verify(scheduleRepository, times(1)).update(any(Schedule.class));
    }

    @Test
    void updateSchedule_shouldThrowException_whenScheduleNotFound() {
        Schedule updateData = Schedule.builder()
                .day(2)
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.empty());

        ScheduleNotFoundException exception = assertThrows(ScheduleNotFoundException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertEquals("[Code: 1003, CodeDescription: RESOURCE_NOT_FOUND, ErrorDescription: Schedule not found.]", exception.getMessage());
        verify(scheduleRepository, never()).update(any(Schedule.class));
    }

    @Test
    void updateSchedule_shouldThrowException_whenDayIsNull() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(null)
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("day")));
    }

    @Test
    void updateSchedule_shouldThrowException_whenDayIsInvalid() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(7)
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("day")));
    }

    @Test
    void updateSchedule_shouldThrowException_whenStartTimeIsNull() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(2)
                .start(null)
                .end(LocalTime.of(11, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("start")));
    }

    @Test
    void updateSchedule_shouldThrowException_whenEndTimeIsNull() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(2)
                .start(LocalTime.of(10, 0))
                .end(null)
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("end")));
    }

    @Test
    void updateSchedule_shouldThrowException_whenEndTimeIsBeforeStartTime() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(2)
                .start(LocalTime.of(12, 0))
                .end(LocalTime.of(11, 0))
                .build();

        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("end")));
    }

    @Test
    void softDeleteSchedules_shouldDeleteSchedules_whenAllBelongToTeacher() {
        List<Integer> ids = Arrays.asList(1, 2, 3);

        when(scheduleRepository.allSchedulesBelongToTeacher(ids, TEACHER_ID)).thenReturn(true);

        scheduleService.softDeleteSchedules(ids);

        verify(scheduleRepository, times(1)).softDeleteSchedules(ids, TEACHER_ID);
    }

    @Test
    void softDeleteSchedules_shouldThrowException_whenIdsListIsNull() {
        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.softDeleteSchedules(null));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("ids")));
        verify(scheduleRepository, never()).softDeleteSchedules(anyList(), anyInt());
    }

    @Test
    void softDeleteSchedules_shouldThrowException_whenIdsListIsEmpty() {
        List<Integer> emptyList = new ArrayList<>();

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.softDeleteSchedules(emptyList));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("ids")));
        verify(scheduleRepository, never()).softDeleteSchedules(anyList(), anyInt());
    }

    @Test
    void softDeleteSchedules_shouldThrowException_whenSomeSchedulesDoNotBelongToTeacher() {
        List<Integer> ids = Arrays.asList(1, 2, 3);

        when(scheduleRepository.allSchedulesBelongToTeacher(ids, TEACHER_ID)).thenReturn(false);

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.softDeleteSchedules(ids));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("ids")));
        verify(scheduleRepository, never()).softDeleteSchedules(anyList(), anyInt());
    }

    @Test
    void createSchedules_shouldSaveMultipleSchedules_whenAllDataIsValid() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = Arrays.asList(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build(),
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build(),
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(12, 0)).end(LocalTime.of(13, 0)).build()
        );

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Schedule> schedules = invocation.getArgument(0);
            int id = 1;
            List<Schedule> result = new ArrayList<>();
            for (Schedule s : schedules) {
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

        List<Schedule> result = scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createSchedules_shouldSetClassIdAndDay_forAllSchedules() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = Arrays.asList(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );

        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Schedule> schedules = invocation.getArgument(0);
            assertEquals(CLASS_ID, schedules.get(0).getClassId());
            assertEquals(3, schedules.get(0).getDay());
            return schedules;
        });

        scheduleService.createSchedules(CLASS_ID, 3, schedulesToCreate);

        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createSchedules_shouldThrowException_whenTimeOverlapsWithExistingSchedule() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 0)).end(LocalTime.of(9, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), any(LocalTime.class), any(LocalTime.class), isNull())).thenReturn(true);

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("time")));
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void createSchedules_shouldThrowException_whenNewScheduleStartsDuringExistingSchedule() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), eq(LocalTime.of(8, 30)), eq(LocalTime.of(9, 30)), isNull())).thenReturn(true);

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("time")));
    }

    @Test
    void createSchedules_shouldThrowException_whenNewScheduleEndsAfterExistingScheduleStarts() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(7, 30)).end(LocalTime.of(8, 30)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), eq(LocalTime.of(7, 30)), eq(LocalTime.of(8, 30)), isNull())).thenReturn(true);

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().contains("time")));
    }

    @Test
    void createSchedules_shouldSave_whenNoTimeOverlapExists() {
        Class classEntity = Class.builder().id(CLASS_ID).build();
        Subject subject = Subject.builder().id(SUBJECT_ID).teacherId(TEACHER_ID).build();
        List<Schedule> schedulesToCreate = List.of(
                Schedule.builder().subjectId(SUBJECT_ID).start(LocalTime.of(10, 0)).end(LocalTime.of(11, 0)).build()
        );
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(classEntity));
        when(subjectRepository.findByIdAndTeacherId(SUBJECT_ID, TEACHER_ID)).thenReturn(Optional.of(subject));
        when(subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(SUBJECT_ID, CLASS_ID)).thenReturn(true);
        when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(1), any(LocalTime.class), any(LocalTime.class), isNull())).thenReturn(false);
        when(scheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Schedule> result = scheduleService.createSchedules(CLASS_ID, 1, schedulesToCreate);

        assertNotNull(result);
        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateSchedule_shouldThrowException_whenTimeOverlapsWithExistingSchedule() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(1)
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.existsOverlappingSchedule(CLASS_ID, 1, LocalTime.of(10, 0), LocalTime.of(11, 0), SCHEDULE_ID)).thenReturn(true);

        ScheduleValidationException exception = assertThrows(ScheduleValidationException.class,
                () -> scheduleService.updateSchedule(SCHEDULE_ID, updateData));

        assertTrue(exception.getErrors().stream().anyMatch(e -> e.getParam().equals("time")));
        verify(scheduleRepository, never()).update(any(Schedule.class));
    }

    @Test
    void updateSchedule_shouldUpdate_whenNoTimeOverlapExists() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(2)
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.existsOverlappingSchedule(CLASS_ID, 2, LocalTime.of(10, 0), LocalTime.of(11, 0), SCHEDULE_ID)).thenReturn(false);
        when(scheduleRepository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule result = scheduleService.updateSchedule(SCHEDULE_ID, updateData);

        assertNotNull(result);
        assertEquals(2, result.getDay());
        verify(scheduleRepository, times(1)).update(any(Schedule.class));
    }

    @Test
    void updateSchedule_shouldNotCheckOverlap_whenDayChangesToDifferentDay() {
        Schedule existingSchedule = Schedule.builder()
                .id(SCHEDULE_ID)
                .classId(CLASS_ID)
                .subjectId(SUBJECT_ID)
                .day(1)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        Schedule updateData = Schedule.builder()
                .day(3)
                .start(LocalTime.of(8, 0))
                .end(LocalTime.of(9, 0))
                .build();
        when(scheduleRepository.findByIdAndTeacherId(SCHEDULE_ID, TEACHER_ID)).thenReturn(Optional.of(existingSchedule));
        when(scheduleRepository.existsOverlappingSchedule(eq(CLASS_ID), eq(3), any(LocalTime.class), any(LocalTime.class), eq(SCHEDULE_ID))).thenReturn(false);
        when(scheduleRepository.update(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule result = scheduleService.updateSchedule(SCHEDULE_ID, updateData);

        assertNotNull(result);
        assertEquals(3, result.getDay());
        verify(scheduleRepository, times(1)).update(any(Schedule.class));
    }
}
