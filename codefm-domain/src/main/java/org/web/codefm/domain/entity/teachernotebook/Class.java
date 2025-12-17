package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Class {
    private Integer id;
    private Integer schoolId;
    private String name;
    private String schoolYear;
    private LocalDate deletionDate;
}
