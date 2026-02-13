package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;

@Data
@Builder
@Generated
public class SubjectClass {
    private Integer id;
    private Integer subjectId;
    private Integer classId;
    private LocalDate deletionDate;
}

