package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Class {
    private Integer id;
    private Integer schoolId;
    private String name;
    private String schoolYear;
}
