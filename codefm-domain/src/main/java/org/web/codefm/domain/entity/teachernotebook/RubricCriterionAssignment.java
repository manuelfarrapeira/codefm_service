package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class RubricCriterionAssignment {
    private Integer id;
    private Integer classRubricId;
    private Integer rubricId;
    private String rubricTitle;
    private Integer criterionId;
    private String criterionDescription;
    private String qualification;
    private Integer gradeStart;
    private Integer gradeEnd;
}

