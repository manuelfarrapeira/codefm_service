package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
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

