package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.domain.repository.teachernotebook.CalendarAlertRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.CalendarAlertEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.CalendarAlertJPARepository;
import org.web.codefm.infrastructure.mapper.CalendarAlertMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CalendarAlertRepositoryImpl implements CalendarAlertRepository {

    private final CalendarAlertJPARepository calendarAlertJPARepository;
    private final CalendarAlertMapper calendarAlertMapper;

    @Override
    public List<CalendarAlert> findByTeacherId(Integer teacherId) {
        return calendarAlertMapper.toModelList(calendarAlertJPARepository.findByTeacherIdOrderByDateAsc(teacherId));
    }

    @Override
    public List<CalendarAlert> findByTeacherIdAndYearAndMonth(Integer teacherId, Integer year, Integer month) {
        return calendarAlertMapper.toModelList(calendarAlertJPARepository.findByTeacherIdAndYearAndMonth(teacherId, year, month));
    }

    @Override
    public List<CalendarAlert> findByTeacherIdAndYearAndMonthRange(Integer teacherId, Integer year, Integer startMonth, Integer endMonth) {
        return calendarAlertMapper.toModelList(calendarAlertJPARepository.findByTeacherIdAndYearAndMonthRange(teacherId, year, startMonth, endMonth));
    }

    @Override
    public Optional<CalendarAlert> findByIdAndTeacherId(Integer id, Integer teacherId) {
        return calendarAlertJPARepository.findByIdAndTeacherId(id, teacherId)
                .map(calendarAlertMapper::toModel);
    }

    @Override
    public CalendarAlert save(CalendarAlert calendarAlert) {
        CalendarAlertEntity entity = calendarAlertMapper.toEntity(calendarAlert);
        CalendarAlertEntity savedEntity = calendarAlertJPARepository.save(entity);
        return calendarAlertMapper.toModel(savedEntity);
    }

    @Override
    public void deleteById(Integer id) {
        calendarAlertJPARepository.deleteById(id);
    }
}

