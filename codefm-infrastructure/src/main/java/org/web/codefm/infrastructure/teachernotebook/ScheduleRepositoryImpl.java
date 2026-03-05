package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.domain.repository.teachernotebook.ScheduleRepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ScheduleJPARepository;
import org.web.codefm.infrastructure.mapper.ScheduleMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepository {

	private final ScheduleJPARepository scheduleJPARepository;
	private final ScheduleMapper scheduleMapper;

	@Override
	public List<Schedule> findByClassId(Integer classId) {
		return scheduleMapper
				.toModelList(scheduleJPARepository.findByClassIdAndDeletionDateIsNullOrderByDayAscStartAsc(classId));
	}

	@Override
	public Optional<Schedule> findById(Integer id) {
		return scheduleJPARepository.findByIdAndDeletionDateIsNull(id).map(scheduleMapper::toModel);
	}

	@Override
	public Optional<Schedule> findByIdAndTeacherId(Integer id, Integer teacherId) {
		return scheduleJPARepository.findByIdAndTeacherId(id, teacherId).map(scheduleMapper::toModel);
	}

	@Override
	public List<Schedule> saveAll(List<Schedule> schedules) {
		var entities = scheduleMapper.toEntityList(schedules);
		var saved = scheduleJPARepository.saveAll(entities);
		return scheduleMapper.toModelList(saved);
	}

	@Override
	public Schedule save(Schedule schedule) {
		var entity = scheduleMapper.toEntity(schedule);
		var saved = scheduleJPARepository.save(entity);
		return scheduleMapper.toModel(saved);
	}

	@Override
	public Schedule update(Schedule schedule) {
		var entity = scheduleMapper.toEntity(schedule);
		var saved = scheduleJPARepository.save(entity);
		return scheduleMapper.toModel(saved);
	}

	@Override
	@Transactional
	public void softDeleteSchedules(List<Integer> ids, Integer teacherId) {
		scheduleJPARepository.softDeleteByIds(ids);
	}

	@Override
	public boolean allSchedulesBelongToTeacher(List<Integer> ids, Integer teacherId) {
		long count = scheduleJPARepository.countByIdsAndTeacherId(ids, teacherId);
		return count == ids.size();
	}

	@Override
	public boolean existsOverlappingSchedule(Integer classId, Integer day, LocalTime start, LocalTime end,
			Integer excludeScheduleId) {
		if (excludeScheduleId != null) {
			return scheduleJPARepository.existsOverlappingScheduleExcluding(classId, day, start, end,
					excludeScheduleId);
		}
		return scheduleJPARepository.existsOverlappingSchedule(classId, day, start, end);
	}

	@Override
	public void softDeleteByClassId(Integer classId) {
		scheduleJPARepository.softDeleteByClassId(classId);
	}

	@Override
	public void softDeleteBySubjectId(Integer subjectId) {
		scheduleJPARepository.softDeleteBySubjectId(subjectId);
	}

	@Override
	public List<Integer> findSubjectIdsByClassIdAndDay(Integer classId, Integer day) {
		return scheduleJPARepository.findDistinctSubjectIdsByClassIdAndDay(classId, day);
	}
}
