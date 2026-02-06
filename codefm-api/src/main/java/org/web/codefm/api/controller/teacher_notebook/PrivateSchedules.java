package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSchedulesApi;
import org.web.codefm.api.mapper.ScheduleDTOMapper;
import org.web.codefm.api.mapper.ScheduleRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.usecase.teachernotebook.ScheduleUseCase;
import org.web.codefm.model.ScheduleCreateRequestDTO;
import org.web.codefm.model.ScheduleDTO;
import org.web.codefm.model.ScheduleDeleteRequestDTO;
import org.web.codefm.model.ScheduleUpdateRequestDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateSchedules implements TeacherNoteBookSchedulesApi {

    private final ScheduleUseCase scheduleUseCase;
    private final ScheduleDTOMapper scheduleDTOMapper;
    private final ScheduleRequestMapper scheduleRequestMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByClass(Integer classId, String acceptLanguage) {
        List<Schedule> schedules = scheduleUseCase.getSchedulesByClassId(classId);
        return ResponseEntity.ok(scheduleDTOMapper.toDTOList(schedules));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ScheduleDTO>> createSchedules(Integer classId, ScheduleCreateRequestDTO scheduleCreateRequestDTO, String acceptLanguage) {
        List<Schedule> schedules = scheduleRequestMapper.toDomainList(scheduleCreateRequestDTO.getItems());
        List<Schedule> created = scheduleUseCase.createSchedules(classId, scheduleCreateRequestDTO.getDay(), schedules);
        return new ResponseEntity<>(scheduleDTOMapper.toDTOList(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ScheduleDTO> updateSchedule(Integer id, ScheduleUpdateRequestDTO scheduleUpdateRequestDTO, String acceptLanguage) {
        Schedule schedule = scheduleRequestMapper.toDomainForUpdate(scheduleUpdateRequestDTO);
        Schedule updated = scheduleUseCase.updateSchedule(id, schedule);
        return ResponseEntity.ok(scheduleDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSchedules(ScheduleDeleteRequestDTO scheduleDeleteRequestDTO, String acceptLanguage) {
        scheduleUseCase.softDeleteSchedules(scheduleDeleteRequestDTO.getIds());
        return ResponseEntity.noContent().build();
    }
}
