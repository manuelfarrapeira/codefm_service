package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Schedule;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ScheduleEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ScheduleJPARepository;
import org.web.codefm.infrastructure.mapper.ScheduleMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleRepositoryImplTest {

    private ScheduleRepositoryImpl scheduleRepository;

    @Mock
    private ScheduleJPARepository scheduleJPARepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @BeforeEach
    void beforeEach() {
        this.scheduleRepository = new ScheduleRepositoryImpl(this.scheduleJPARepository, this.scheduleMapper);
    }

    @Nested
    class FindByClassId {

        @Test
        void when_schedules_exist_expect_schedules_returned() {
            final Integer classId = 1;
            final ScheduleEntity entity = new ScheduleEntity(1, classId, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
            final Schedule schedule = Schedule.builder().id(1).classId(classId).build();

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.findByClassIdAndDeletionDateIsNullOrderByDayAscStartAsc(classId)).thenReturn(List.of(entity));
            when(ScheduleRepositoryImplTest.this.scheduleMapper.toModelList(List.of(entity))).thenReturn(List.of(schedule));

            final List<Schedule> result = ScheduleRepositoryImplTest.this.scheduleRepository.findByClassId(classId);

            assertThat(result).isNotNull().hasSize(1);
            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository, times(1))
                    .findByClassIdAndDeletionDateIsNullOrderByDayAscStartAsc(classId);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_schedule_is_found_expect_schedule_returned() {
            final Integer id = 1;
            final ScheduleEntity entity = new ScheduleEntity(id, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
            final Schedule schedule = Schedule.builder().id(id).build();

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.findByIdAndDeletionDateIsNull(id)).thenReturn(Optional.of(entity));
            when(ScheduleRepositoryImplTest.this.scheduleMapper.toModel(entity)).thenReturn(schedule);

            final Optional<Schedule> result = ScheduleRepositoryImplTest.this.scheduleRepository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
        }

        @Test
        void when_schedule_is_not_found_expect_empty_optional_returned() {
            final Integer id = 99;

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.findByIdAndDeletionDateIsNull(id)).thenReturn(Optional.empty());

            final Optional<Schedule> result = ScheduleRepositoryImplTest.this.scheduleRepository.findById(id);

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_schedule_is_found_and_owned_expect_schedule_returned() {
            final Integer id = 1;
            final Integer teacherId = 1;
            final ScheduleEntity entity = new ScheduleEntity(id, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
            final Schedule schedule = Schedule.builder().id(id).build();

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
            when(ScheduleRepositoryImplTest.this.scheduleMapper.toModel(entity)).thenReturn(schedule);

            final Optional<Schedule> result = ScheduleRepositoryImplTest.this.scheduleRepository.findByIdAndTeacherId(id, teacherId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
        }
    }

    @Nested
    class SaveAll {

        @Test
        void when_valid_schedules_expect_schedules_saved() {
            final Schedule schedule = Schedule.builder().subjectId(1).day(1).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
            final ScheduleEntity entity = new ScheduleEntity();
            final ScheduleEntity savedEntity = new ScheduleEntity(1, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
            final Schedule savedSchedule = Schedule.builder().id(1).build();

            when(ScheduleRepositoryImplTest.this.scheduleMapper.toEntityList(List.of(schedule))).thenReturn(List.of(entity));
            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.saveAll(List.of(entity))).thenReturn(List.of(savedEntity));
            when(ScheduleRepositoryImplTest.this.scheduleMapper.toModelList(List.of(savedEntity))).thenReturn(List.of(savedSchedule));

            final List<Schedule> result = ScheduleRepositoryImplTest.this.scheduleRepository.saveAll(List.of(schedule));

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1);
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_schedule_expect_schedule_saved() {
            final Schedule schedule = Schedule.builder().subjectId(1).day(1).start(LocalTime.of(8, 30)).end(LocalTime.of(9, 30)).build();
            final ScheduleEntity entity = new ScheduleEntity();
            final ScheduleEntity savedEntity = new ScheduleEntity(1, 1, 1, 1, LocalTime.of(8, 30), LocalTime.of(9, 30), null);
            final Schedule savedSchedule = Schedule.builder().id(1).build();

            when(ScheduleRepositoryImplTest.this.scheduleMapper.toEntity(schedule)).thenReturn(entity);
            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.save(entity)).thenReturn(savedEntity);
            when(ScheduleRepositoryImplTest.this.scheduleMapper.toModel(savedEntity)).thenReturn(savedSchedule);

            final Schedule result = ScheduleRepositoryImplTest.this.scheduleRepository.save(schedule);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
        }
    }

    @Nested
    class Update {

        @Test
        void when_valid_schedule_expect_schedule_updated() {
            final Schedule schedule = Schedule.builder().id(1).subjectId(1).day(2).start(LocalTime.of(9, 0)).end(LocalTime.of(10, 0)).build();
            final ScheduleEntity entity = new ScheduleEntity(1, 1, 1, 2, LocalTime.of(9, 0), LocalTime.of(10, 0), null);
            final Schedule updatedSchedule = Schedule.builder().id(1).day(2).build();

            when(ScheduleRepositoryImplTest.this.scheduleMapper.toEntity(schedule)).thenReturn(entity);
            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.save(entity)).thenReturn(entity);
            when(ScheduleRepositoryImplTest.this.scheduleMapper.toModel(entity)).thenReturn(updatedSchedule);

            final Schedule result = ScheduleRepositoryImplTest.this.scheduleRepository.update(schedule);

            assertThat(result).isNotNull();
            assertThat(result.getDay()).isEqualTo(2);
        }
    }

    @Nested
    class AllSchedulesBelongToTeacher {

        @Test
        void when_all_schedules_belong_expect_true() {
            final List<Integer> ids = List.of(1, 2, 3);
            final Integer teacherId = 1;

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.countByIdsAndTeacherId(ids, teacherId)).thenReturn(3L);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.allSchedulesBelongToTeacher(ids, teacherId);

            assertThat(result).isTrue();
        }

        @Test
        void when_not_all_schedules_belong_expect_false() {
            final List<Integer> ids = List.of(1, 2, 3);
            final Integer teacherId = 1;

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.countByIdsAndTeacherId(ids, teacherId)).thenReturn(2L);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.allSchedulesBelongToTeacher(ids, teacherId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class SoftDeleteSchedules {

        @Test
        void when_called_expect_soft_delete_executed() {
            final List<Integer> ids = List.of(1, 2, 3);
            final Integer teacherId = 1;

            ScheduleRepositoryImplTest.this.scheduleRepository.softDeleteSchedules(ids, teacherId);

            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository, times(1)).softDeleteByIds(ids);
        }
    }

    @Nested
    class ExistsOverlappingSchedule {

        @Test
        void when_exclude_id_is_not_null_expect_repository_called_with_exclusion() {
            final Integer classId = 1;
            final Integer day = 2;
            final LocalTime start = LocalTime.of(8, 30);
            final LocalTime end = LocalTime.of(9, 30);
            final Integer excludeId = 10;

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.existsOverlappingScheduleExcluding(classId, day, start, end, excludeId)).thenReturn(true);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.existsOverlappingSchedule(classId, day, start, end, excludeId);

            assertThat(result).isTrue();
            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository)
                    .existsOverlappingScheduleExcluding(classId, day, start, end, excludeId);
            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository, never()).existsOverlappingSchedule(any(), any(), any(), any());
        }

        @Test
        void when_exclude_id_is_null_expect_repository_called_without_exclusion() {
            final Integer classId = 1;
            final Integer day = 2;
            final LocalTime start = LocalTime.of(8, 30);
            final LocalTime end = LocalTime.of(9, 30);

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.existsOverlappingSchedule(classId, day, start, end)).thenReturn(false);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.existsOverlappingSchedule(classId, day, start, end, null);

            assertThat(result).isFalse();
            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository).existsOverlappingSchedule(classId, day, start, end);
            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository, never())
                    .existsOverlappingScheduleExcluding(any(), any(), any(), any(), any());
        }

        @Test
        void when_no_overlap_exists_expect_false() {
            final Integer classId = 1;
            final Integer day = 3;
            final LocalTime start = LocalTime.of(10, 0);
            final LocalTime end = LocalTime.of(11, 0);

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.existsOverlappingSchedule(classId, day, start, end)).thenReturn(false);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.existsOverlappingSchedule(classId, day, start, end, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class SoftDeleteByClassId {

        @Test
        void when_called_expect_repository_delegated() {
            ScheduleRepositoryImplTest.this.scheduleRepository.softDeleteByClassId(10);

            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository).softDeleteByClassId(10);
        }
    }

    @Nested
    class SoftDeleteBySubjectId {

        @Test
        void when_called_expect_repository_delegated() {
            ScheduleRepositoryImplTest.this.scheduleRepository.softDeleteBySubjectId(5);

            verify(ScheduleRepositoryImplTest.this.scheduleJPARepository).softDeleteBySubjectId(5);
        }
    }

    @Nested
    class FindSubjectIdsByClassIdAndDay {

        @Test
        void when_subject_ids_exist_expect_subject_ids_returned() {
            final Integer classId = 1;
            final Integer day = 2;

            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.findDistinctSubjectIdsByClassIdAndDay(classId, day))
                    .thenReturn(List.of(10, 20, 30));

            final List<Integer> result = ScheduleRepositoryImplTest.this.scheduleRepository.findSubjectIdsByClassIdAndDay(classId, day);

            assertThat(result).isNotNull().hasSize(3).isEqualTo(List.of(10, 20, 30));
        }

        @Test
        void when_no_subject_ids_exist_expect_empty_list_returned() {
            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.findDistinctSubjectIdsByClassIdAndDay(1, 5)).thenReturn(List.of());

            final List<Integer> result = ScheduleRepositoryImplTest.this.scheduleRepository.findSubjectIdsByClassIdAndDay(1, 5);

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    class ExistsByClassIdAndSubjectIdAndDay {

        @Test
        void when_schedule_exists_expect_true() {
            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.existsByClassIdAndSubjectIdAndDay(1, 10, 2)).thenReturn(true);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.existsByClassIdAndSubjectIdAndDay(1, 10, 2);

            assertThat(result).isTrue();
        }

        @Test
        void when_schedule_does_not_exist_expect_false() {
            when(ScheduleRepositoryImplTest.this.scheduleJPARepository.existsByClassIdAndSubjectIdAndDay(1, 10, 2)).thenReturn(false);

            final boolean result = ScheduleRepositoryImplTest.this.scheduleRepository.existsByClassIdAndSubjectIdAndDay(1, 10, 2);

            assertThat(result).isFalse();
        }
    }
}
