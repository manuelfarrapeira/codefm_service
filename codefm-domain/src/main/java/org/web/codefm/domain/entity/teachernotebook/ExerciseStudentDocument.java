package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class ExerciseStudentDocument {
    private Integer id;
    private Integer gradeId;
    private String document;
    private String description;
}

