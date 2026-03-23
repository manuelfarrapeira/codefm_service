package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Generated
public class SavedStudentGroup {
    private Integer id;
    private Integer classId;
    private String name;
    private LocalDate deletionDate;
    private List<SavedStudentGroupMember> members;
}
