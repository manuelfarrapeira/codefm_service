package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class ExerciseDocument {
    private Integer id;
    private Integer exerciseId;
    private String document;
    private String description;
}

