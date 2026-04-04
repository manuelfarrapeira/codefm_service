package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Generated
public class GroupAssignmentGrade {
    private Integer id;
    private Integer groupAssignmentId;
    private Integer groupId;
    private Double grade;
    private String groupName;
    private List<GroupAssignmentDocument> documents;
    private LocalDate deletionDate;
}

