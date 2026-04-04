package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Generated
public class GroupAssignment {
    private Integer id;
    private Integer classId;
    private String title;
    private String description;
    private Integer quarter;
    private List<GroupAssignmentDocument> documents;
    private LocalDate deletionDate;
}

