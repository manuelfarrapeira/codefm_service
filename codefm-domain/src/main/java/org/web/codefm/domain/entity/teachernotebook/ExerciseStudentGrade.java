package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ExerciseStudentGrade {
    private Integer id;
    private Integer studentId;
    private Integer exerciseId;
    private Double grade;
    private String description;
    private LocalDate deletionDate;
    private String studentName;
    private String studentSurnames;
    private String exerciseTitle;
    private Integer subjectId;
    private String subjectName;
    private Integer quarter;
    private Integer maxGrade;
    private Integer percentageGrade;
    private List<ExerciseStudentDocument> documents;
}
