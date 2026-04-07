package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class StudentClassRubricCriteria {
    private Integer id;
    private Integer classRubricId;
    private Integer studentId;
    private Integer criterionId;
    private LocalDate deletionDate;
    private Integer rubricId;
    private String rubricTitle;
    private String studentName;
    private String studentSurnames;
    private String criterionDescription;
    private String qualification;
    private Integer gradeStart;
    private Integer gradeEnd;
}

