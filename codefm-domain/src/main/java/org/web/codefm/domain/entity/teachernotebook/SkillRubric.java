package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Generated
public class SkillRubric {
    private Integer id;
    private String title;
    private Integer skillId;
    private LocalDate deletionDate;
    private List<SkillRubricCriteria> criteria;
}

