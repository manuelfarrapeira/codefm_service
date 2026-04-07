package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SubjectClass {
    private Integer id;
    private Integer subjectId;
    private Integer classId;
    private LocalDate deletionDate;
}

