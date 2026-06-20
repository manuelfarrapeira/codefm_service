package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.domain.entity.teachernotebook.CalendarAlert;
import org.web.codefm.model.CalendarAlertDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarAlertDTOMapperTest {

    private final CalendarAlertDTOMapper mapper = new CalendarAlertDTOMapperImpl();

    @Nested
    class ToDTO {

        @Test
        void when_all_fields_are_present_expect_mapped_dto() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Parent meeting")
                    .description("Quarterly meeting")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 30))
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getDate()).isEqualTo("15/03/2026");
            assertThat(result.getTitle()).isEqualTo("Parent meeting");
            assertThat(result.getDescription()).isEqualTo("Quarterly meeting");
            assertThat(result.getStartTime()).isEqualTo("09:00");
            assertThat(result.getEndTime()).isEqualTo("10:30");
        }

        @Test
        void when_day_and_month_have_single_digits_expect_padded_date() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 1, 5))
                    .title("Meeting")
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result.getDate()).isEqualTo("05/01/2026");
        }

        @Test
        void when_date_is_null_expect_null_date() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(null)
                    .title("Meeting")
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result).isNotNull();
            assertThat(result.getDate()).isNull();
        }

        @Test
        void when_start_time_is_null_expect_null_start_time() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Meeting")
                    .startTime(null)
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result).isNotNull();
            assertThat(result.getStartTime()).isNull();
        }

        @Test
        void when_end_time_is_null_expect_null_end_time() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Meeting")
                    .endTime(null)
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result).isNotNull();
            assertThat(result.getEndTime()).isNull();
        }

        @Test
        void when_description_is_null_expect_null_description() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Meeting")
                    .description(null)
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isNull();
        }

        @Test
        void when_start_time_is_midnight_expect_formatted_midnight() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Meeting")
                    .startTime(LocalTime.of(0, 0))
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result.getStartTime()).isEqualTo("00:00");
        }

        @Test
        void when_end_time_is_end_of_day_expect_formatted_times() {
            final CalendarAlert alert = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Meeting")
                    .startTime(LocalTime.of(22, 0))
                    .endTime(LocalTime.of(23, 59))
                    .build();

            final CalendarAlertDTO result = CalendarAlertDTOMapperTest.this.mapper.toDTO(alert);

            assertThat(result.getStartTime()).isEqualTo("22:00");
            assertThat(result.getEndTime()).isEqualTo("23:59");
        }
    }

    @Nested
    class ToDTOList {

        @Test
        void when_input_has_alerts_expect_mapped_list() {
            final CalendarAlert alert1 = CalendarAlert.builder()
                    .id(1)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 3, 15))
                    .title("Meeting A")
                    .startTime(LocalTime.of(9, 0))
                    .build();
            final CalendarAlert alert2 = CalendarAlert.builder()
                    .id(2)
                    .teacherId(10)
                    .date(LocalDate.of(2026, 4, 20))
                    .title("Meeting B")
                    .startTime(LocalTime.of(14, 30))
                    .endTime(LocalTime.of(15, 0))
                    .build();

            final List<CalendarAlertDTO> result = CalendarAlertDTOMapperTest.this.mapper.toDTOList(Arrays.asList(alert1, alert2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getDate()).isEqualTo("15/03/2026");
            assertThat(result.get(0).getTitle()).isEqualTo("Meeting A");
            assertThat(result.get(0).getStartTime()).isEqualTo("09:00");
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getDate()).isEqualTo("20/04/2026");
            assertThat(result.get(1).getTitle()).isEqualTo("Meeting B");
            assertThat(result.get(1).getStartTime()).isEqualTo("14:30");
            assertThat(result.get(1).getEndTime()).isEqualTo("15:00");
        }

        @Test
        void when_input_is_empty_expect_empty_list() {
            assertThat(CalendarAlertDTOMapperTest.this.mapper.toDTOList(List.of())).isEmpty();
        }
    }
}
