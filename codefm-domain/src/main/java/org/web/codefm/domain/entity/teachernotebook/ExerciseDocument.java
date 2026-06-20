package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ExerciseDocument {
    private Integer id;
    private Integer exerciseId;
    private String document;
    private String description;
}

