package org.web.codefm.application.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.exception.teachernotebook.ScheduleNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ScheduleValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.ScheduleRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.domain.service.teachernotebook.ScheduleService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private static final String FIELD_SUBJECT_ID = "subjectId";

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<Schedule> getSchedulesByClassId(Integer classId) {
        Integer teacherId = getTeacherId();

        classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ScheduleNotFoundException(
                        messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_CLASS_NOT_FOUND, null, sessionUser.getLocale())
                ));

        return scheduleRepository.findByClassId(classId);
    }

    @Override
    public List<Schedule> createSchedules(Integer classId, Integer day, List<Schedule> schedules) {
        Integer teacherId = getTeacherId();
        List<ErrorMessage> errors = new ArrayList<>();

        classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ScheduleNotFoundException(
                        messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_CLASS_NOT_FOUND, null, sessionUser.getLocale())
                ));

        validateDay(day, errors);

        if (schedules == null || schedules.isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_ITEMS_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("items", message));
            throw new ScheduleValidationException(errors);
        }

        if (!errors.isEmpty()) {
            throw new ScheduleValidationException(errors);
        }

        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);
            validateScheduleItem(schedule, i, classId, teacherId, errors);
            validateTimeOverlapForCreate(classId, day, schedule, i, errors);
        }

        if (!errors.isEmpty()) {
            throw new ScheduleValidationException(errors);
        }

        List<Schedule> schedulesToSave = new ArrayList<>();
        for (Schedule schedule : schedules) {
            Schedule scheduleToSave = Schedule.builder()
                    .classId(classId)
                    .subjectId(schedule.getSubjectId())
                    .day(day)
                    .start(schedule.getStart())
                    .end(schedule.getEnd())
                    .build();
            schedulesToSave.add(scheduleToSave);
        }

        return scheduleRepository.saveAll(schedulesToSave);
    }

    @Override
    public Schedule updateSchedule(Integer scheduleId, Schedule schedule) {
        Integer teacherId = getTeacherId();
        List<ErrorMessage> errors = new ArrayList<>();

        Schedule existingSchedule = scheduleRepository.findByIdAndTeacherId(scheduleId, teacherId)
                .orElseThrow(() -> new ScheduleNotFoundException(
                        messageSource.getMessage(MessageKeys.SCHEDULE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        validateDay(schedule.getDay(), errors);
        validateStartEnd(schedule, errors);
        validateTimeOverlapForUpdate(existingSchedule, schedule, errors);

        if (!errors.isEmpty()) {
            throw new ScheduleValidationException(errors);
        }

        existingSchedule.setDay(schedule.getDay());
        existingSchedule.setStart(schedule.getStart());
        existingSchedule.setEnd(schedule.getEnd());

        return scheduleRepository.update(existingSchedule);
    }

    @Override
    public void softDeleteSchedules(List<Integer> ids) {
        Integer teacherId = getTeacherId();
        List<ErrorMessage> errors = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_IDS_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("ids", message));
            throw new ScheduleValidationException(errors);
        }

        if (!scheduleRepository.allSchedulesBelongToTeacher(ids, teacherId)) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_IDS_NOT_OWNED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("ids", message));
            throw new ScheduleValidationException(errors);
        }

        scheduleRepository.softDeleteSchedules(ids, teacherId);
    }

    private void validateDay(Integer day, List<ErrorMessage> errors) {
        if (day == null) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_DAY_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("day", message));
        } else if (day < 1 || day > 5) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_DAY_INVALID, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("day", message));
        }
    }


    private void validateStartEnd(Schedule schedule, List<ErrorMessage> errors) {
        if (schedule.getStart() == null) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_START_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("start", message));
        }

        if (schedule.getEnd() == null) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_END_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("end", message));
        }

        if (schedule.getStart() != null && schedule.getEnd() != null && !schedule.getEnd().isAfter(schedule.getStart())) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_END_BEFORE_START, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("end", message));
        }
    }

    private void validateScheduleItem(Schedule schedule, int index, Integer classId, Integer teacherId, List<ErrorMessage> errors) {
        String prefix = "items[" + index + "].";

        if (schedule.getSubjectId() == null) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_SUBJECT_NOT_FOUND, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(prefix + FIELD_SUBJECT_ID, message));
        } else {
            var subjectOpt = subjectRepository.findByIdAndTeacherId(schedule.getSubjectId(), teacherId);
            if (subjectOpt.isEmpty()) {
                String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_SUBJECT_NOT_FOUND, null, sessionUser.getLocale());
                errors.add(new ErrorMessage(prefix + FIELD_SUBJECT_ID, message));
            } else if (!subjectClassRepository.existsBySubjectIdAndClassIdAndDeletionDateIsNull(schedule.getSubjectId(), classId)) {
                String subjectName = subjectOpt.map(Subject::getName).orElse(String.valueOf(schedule.getSubjectId()));
                String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_SUBJECT_NOT_IN_CLASS, new Object[]{subjectName}, sessionUser.getLocale());
                errors.add(new ErrorMessage(prefix + FIELD_SUBJECT_ID, message));
            }
        }

        if (schedule.getStart() == null) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_START_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(prefix + "start", message));
        }

        if (schedule.getEnd() == null) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_END_REQUIRED, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(prefix + "end", message));
        }

        if (schedule.getStart() != null && schedule.getEnd() != null && !schedule.getEnd().isAfter(schedule.getStart())) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_END_BEFORE_START, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(prefix + "end", message));
        }
    }

    private void validateTimeOverlapForCreate(Integer classId, Integer day, Schedule schedule, int index, List<ErrorMessage> errors) {
        if (schedule.getStart() == null || schedule.getEnd() == null || day == null) {
            return;
        }
        String prefix = "items[" + index + "].";
        if (scheduleRepository.existsOverlappingSchedule(classId, day, schedule.getStart(), schedule.getEnd(), null)) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_TIME_OVERLAP, null, sessionUser.getLocale());
            errors.add(new ErrorMessage(prefix + "time", message));
        }
    }

    private void validateTimeOverlapForUpdate(Schedule existingSchedule, Schedule updateData, List<ErrorMessage> errors) {
        if (updateData.getStart() == null || updateData.getEnd() == null || updateData.getDay() == null) {
            return;
        }
        if (scheduleRepository.existsOverlappingSchedule(existingSchedule.getClassId(), updateData.getDay(), updateData.getStart(), updateData.getEnd(), existingSchedule.getId())) {
            String message = messageSource.getMessage(MessageKeys.SCHEDULE_VALIDATION_TIME_OVERLAP, null, sessionUser.getLocale());
            errors.add(new ErrorMessage("time", message));
        }
    }

    private Integer getTeacherId() {
        return Integer.valueOf(
                sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }
}
