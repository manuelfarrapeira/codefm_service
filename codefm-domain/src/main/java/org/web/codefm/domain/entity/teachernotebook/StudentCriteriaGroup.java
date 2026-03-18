package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.util.List;

@Data
@Builder
@Generated
public class StudentCriteriaGroup {
    private Integer studentId;
    private String studentName;
    private String studentSurnames;
    private List<RubricCriterionAssignment> rubricCriteria;
}

