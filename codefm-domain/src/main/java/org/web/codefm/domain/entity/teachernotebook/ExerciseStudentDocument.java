package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ExerciseStudentDocument {
    private Integer id;
    private Integer gradeId;
    private String document;
    private String description;
}

