package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;

@Data
@Builder
@Generated
public class Subject {
    private Integer id;
    private String name;
    private Integer teacherId;
    private LocalDate deletionDate;
}
