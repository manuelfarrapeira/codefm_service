package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SkillRubricCriteria {
    private Integer id;
    private String description;
    private String qualification;
    private Integer rubricId;
    private Integer gradeStart;
    private Integer gradeEnd;
    private LocalDate deletionDate;
}

