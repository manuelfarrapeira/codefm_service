package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@Generated
public class Schedule {
    private Integer id;
    private Integer classId;
    private Integer subjectId;
    private Integer day;
    private LocalTime start;
    private LocalTime end;
    private LocalDate deletionDate;
}
