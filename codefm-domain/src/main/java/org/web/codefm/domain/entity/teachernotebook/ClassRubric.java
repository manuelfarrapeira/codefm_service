package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ClassRubric {
    private Integer id;
    private Integer classId;
    private Integer rubricId;
    private LocalDate deletionDate;
    private String rubricTitle;
    private Integer skillId;
    private List<SkillRubricCriteria> criteria;
}

