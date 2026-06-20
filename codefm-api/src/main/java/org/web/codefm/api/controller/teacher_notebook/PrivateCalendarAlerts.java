package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookCalendarAlertsApi;
import org.web.codefm.api.mapper.CalendarAlertDTOMapper;
import org.web.codefm.api.mapper.CalendarAlertRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.usecase.teachernotebook.CalendarAlertUseCase;
import org.web.codefm.model.CalendarAlertDTO;
import org.web.codefm.model.CalendarAlertRequestDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateCalendarAlerts implements TeacherNoteBookCalendarAlertsApi {

    private final CalendarAlertUseCase calendarAlertUseCase;
    private final CalendarAlertDTOMapper calendarAlertDTOMapper;
    private final CalendarAlertRequestMapper calendarAlertRequestMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CalendarAlertDTO>> calendarAlerts() {
        return ResponseEntity.ok(calendarAlertDTOMapper.toDTOList(calendarAlertUseCase.getCalendarAlerts()));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CalendarAlertDTO>> calendarAlertsByYearAndMonth(Integer year, Integer month, String acceptLanguage) {
        return ResponseEntity.ok(calendarAlertDTOMapper.toDTOList(calendarAlertUseCase.getCalendarAlertsByYearAndMonth(year, month)));
    }

    @Logged
    @Override
    @Locale(3)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CalendarAlertDTO>> calendarAlertsByYearAndMonthRange(Integer year, Integer startMonth, Integer endMonth, String acceptLanguage) {
        return ResponseEntity.ok(this.calendarAlertDTOMapper.toDTOList(this.calendarAlertUseCase.getCalendarAlertsByYearAndMonthRange(year, startMonth, endMonth)));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CalendarAlertDTO> createCalendarAlert(CalendarAlertRequestDTO calendarAlertRequestDTO, String acceptLanguage) {
        CalendarAlert calendarAlert = calendarAlertRequestMapper.toDomain(calendarAlertRequestDTO);
        CalendarAlert created = calendarAlertUseCase.createCalendarAlert(calendarAlert);
        return new ResponseEntity<>(calendarAlertDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CalendarAlertDTO> updateCalendarAlert(Integer id, CalendarAlertRequestDTO calendarAlertRequestDTO, String acceptLanguage) {
        CalendarAlert calendarAlert = calendarAlertRequestMapper.toDomain(calendarAlertRequestDTO);
        CalendarAlert updated = calendarAlertUseCase.updateCalendarAlert(id, calendarAlert);
        return ResponseEntity.ok(calendarAlertDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteCalendarAlert(Integer id, String acceptLanguage) {
        calendarAlertUseCase.deleteCalendarAlert(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

