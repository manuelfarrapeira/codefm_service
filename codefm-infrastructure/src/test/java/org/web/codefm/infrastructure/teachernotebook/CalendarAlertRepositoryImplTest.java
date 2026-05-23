package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.CalendarAlertEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.CalendarAlertJPARepository;
import org.web.codefm.infrastructure.mapper.CalendarAlertMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarAlertRepositoryImplTest {

    private CalendarAlertRepositoryImpl calendarAlertRepository;

    @Mock
    private CalendarAlertJPARepository calendarAlertJPARepository;

    @Mock
    private CalendarAlertMapper calendarAlertMapper;

    @BeforeEach
    void beforeEach() {
        this.calendarAlertRepository = new CalendarAlertRepositoryImpl(this.calendarAlertJPARepository, this.calendarAlertMapper);
    }

    @Nested
    class FindByTeacherId {

        @Test
        void when_teacher_has_alerts_expect_alerts_returned() {
            final Integer teacherId = 1;
            final CalendarAlertEntity entity1 = new CalendarAlertEntity(1, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null,
                    null);
            final CalendarAlertEntity entity2 = new CalendarAlertEntity(2, teacherId, LocalDate.of(2026, 3, 20), "Exam", null, null, null);
            final List<CalendarAlertEntity> entities = Arrays.asList(entity1, entity2);
            final CalendarAlert alert1 = CalendarAlert.builder().id(1).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting")
                    .build();
            final CalendarAlert alert2 = CalendarAlert.builder().id(2).teacherId(teacherId).date(LocalDate.of(2026, 3, 20)).title("Exam")
                    .build();
            final List<CalendarAlert> expectedAlerts = Arrays.asList(alert1, alert2);

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByTeacherIdOrderByDateAsc(teacherId)).thenReturn(entities);
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModelList(entities)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().hasSize(2);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByTeacherIdOrderByDateAsc(teacherId);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertMapper).toModelList(entities);
        }

        @Test
        void when_teacher_has_no_alerts_expect_empty_list_returned() {
            final Integer teacherId = 1;

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByTeacherIdOrderByDateAsc(teacherId)).thenReturn(
                    Collections.emptyList());
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModelList(Collections.emptyList())).thenReturn(
                    Collections.emptyList());

            final List<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByTeacherIdOrderByDateAsc(teacherId);
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_alert_exists_expect_alert_returned() {
            final Integer id = 1;
            final Integer teacherId = 1;
            final CalendarAlertEntity entity = new CalendarAlertEntity(id, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
            final CalendarAlert alert = CalendarAlert.builder().id(id).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting")
                    .build();

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.of(entity));
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModel(entity)).thenReturn(alert);

            final Optional<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByIdAndTeacherId(id,
                    teacherId);

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Meeting");
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByIdAndTeacherId(id, teacherId);
        }

        @Test
        void when_alert_does_not_exist_expect_empty_optional_returned() {
            final Integer id = 999;
            final Integer teacherId = 1;

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByIdAndTeacherId(id, teacherId)).thenReturn(Optional.empty());

            final Optional<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByIdAndTeacherId(id,
                    teacherId);

            assertThat(result).isNotPresent();
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByIdAndTeacherId(id, teacherId);
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_entity_saved() {
            final CalendarAlert alertToSave = CalendarAlert.builder().teacherId(1).date(LocalDate.of(2026, 3, 15)).title("New alert").build();
            final CalendarAlertEntity entity = new CalendarAlertEntity();
            final CalendarAlertEntity savedEntity = new CalendarAlertEntity(1, 1, LocalDate.of(2026, 3, 15), "New alert", null, null, null);
            final CalendarAlert savedAlert = CalendarAlert.builder().id(1).teacherId(1).date(LocalDate.of(2026, 3, 15)).title("New alert")
                    .build();

            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toEntity(alertToSave)).thenReturn(entity);
            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.save(entity)).thenReturn(savedEntity);
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModel(savedEntity)).thenReturn(savedAlert);

            final CalendarAlert result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.save(alertToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getTitle()).isEqualTo("New alert");
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertMapper).toEntity(alertToSave);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).save(entity);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertMapper).toModel(savedEntity);
        }
    }

    @Nested
    class DeleteById {

        @Test
        void when_id_provided_expect_repository_delete_called() {
            final Integer id = 1;

            doNothing().when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).deleteById(id);

            CalendarAlertRepositoryImplTest.this.calendarAlertRepository.deleteById(id);

            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).deleteById(id);
        }
    }

    @Nested
    class FindByTeacherIdAndYearAndMonth {

        @Test
        void when_alerts_exist_expect_alerts_returned() {
            final Integer teacherId = 1;
            final Integer year = 2026;
            final Integer month = 3;
            final CalendarAlertEntity entity1 = new CalendarAlertEntity(1, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
            final List<CalendarAlertEntity> entities = List.of(entity1);
            final CalendarAlert alert1 = CalendarAlert.builder().id(1).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting")
                    .build();
            final List<CalendarAlert> expectedAlerts = List.of(alert1);

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByTeacherIdAndYearAndMonth(teacherId, year, month))
                    .thenReturn(entities);
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModelList(entities)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByTeacherIdAndYearAndMonth(
                    teacherId, year, month);

            assertThat(result).isNotNull().hasSize(1);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByTeacherIdAndYearAndMonth(teacherId, year, month);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertMapper).toModelList(entities);
        }

        @Test
        void when_alerts_do_not_exist_expect_empty_list_returned() {
            final Integer teacherId = 1;
            final Integer year = 2026;
            final Integer month = 6;

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByTeacherIdAndYearAndMonth(teacherId, year, month))
                    .thenReturn(Collections.emptyList());
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModelList(Collections.emptyList())).thenReturn(
                    Collections.emptyList());

            final List<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByTeacherIdAndYearAndMonth(
                    teacherId, year, month);

            assertThat(result).isNotNull().isEmpty();
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByTeacherIdAndYearAndMonth(teacherId, year, month);
        }
    }

    @Nested
    class FindByTeacherIdAndYearAndMonthRange {

        @Test
        void when_alerts_exist_in_range_expect_alerts_returned() {
            final Integer teacherId = 1;
            final Integer year = 2026;
            final Integer startMonth = 1;
            final Integer endMonth = 6;
            final CalendarAlertEntity entity1 = new CalendarAlertEntity(1, teacherId, LocalDate.of(2026, 3, 15), "Meeting", null, null, null);
            final CalendarAlertEntity entity2 = new CalendarAlertEntity(2, teacherId, LocalDate.of(2026, 5, 10), "Exam", null, null, null);
            final List<CalendarAlertEntity> entities = Arrays.asList(entity1, entity2);
            final CalendarAlert alert1 = CalendarAlert.builder().id(1).teacherId(teacherId).date(LocalDate.of(2026, 3, 15)).title("Meeting")
                    .build();
            final CalendarAlert alert2 = CalendarAlert.builder().id(2).teacherId(teacherId).date(LocalDate.of(2026, 5, 10)).title("Exam")
                    .build();
            final List<CalendarAlert> expectedAlerts = Arrays.asList(alert1, alert2);

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByTeacherIdAndYearAndMonthRange(teacherId, year,
                    startMonth, endMonth)).thenReturn(entities);
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModelList(entities)).thenReturn(expectedAlerts);

            final List<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(
                    teacherId, year, startMonth, endMonth);

            assertThat(result).isNotNull().hasSize(2);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByTeacherIdAndYearAndMonthRange(teacherId, year,
                    startMonth, endMonth);
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertMapper).toModelList(entities);
        }

        @Test
        void when_alerts_do_not_exist_in_range_expect_empty_list_returned() {
            final Integer teacherId = 1;
            final Integer year = 2099;
            final Integer startMonth = 1;
            final Integer endMonth = 12;

            when(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository.findByTeacherIdAndYearAndMonthRange(teacherId, year,
                    startMonth, endMonth)).thenReturn(Collections.emptyList());
            when(CalendarAlertRepositoryImplTest.this.calendarAlertMapper.toModelList(Collections.emptyList())).thenReturn(
                    Collections.emptyList());

            final List<CalendarAlert> result = CalendarAlertRepositoryImplTest.this.calendarAlertRepository.findByTeacherIdAndYearAndMonthRange(
                    teacherId, year, startMonth, endMonth);

            assertThat(result).isNotNull().isEmpty();
            verify(CalendarAlertRepositoryImplTest.this.calendarAlertJPARepository).findByTeacherIdAndYearAndMonthRange(teacherId, year,
                    startMonth, endMonth);
        }
    }
}
