package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;

@Data
@Builder
@Generated
public class Skill {
    private Integer id;
    private String title;
    private String description;
    private Integer teacherId;
    private LocalDate deletionDate;
}

