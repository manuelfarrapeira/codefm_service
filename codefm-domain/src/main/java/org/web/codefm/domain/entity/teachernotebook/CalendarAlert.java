package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@Generated
public class CalendarAlert {
    private Integer id;
    private Integer teacherId;
    private LocalDate date;
    private String title;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
}

