package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class GroupAssignmentDocument {
    private Integer id;
    private Integer groupAssignmentId;
    private Integer groupId;
    private String document;
    private String description;
    private Boolean groupDocument;
}

