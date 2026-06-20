package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

@Data
@Builder
@Generated
public class SavedStudentGroupMember {
    private Integer id;
    private Integer studentGroupId;
    private Integer studentId;
    private String studentName;
    private String studentSurnames;
}
