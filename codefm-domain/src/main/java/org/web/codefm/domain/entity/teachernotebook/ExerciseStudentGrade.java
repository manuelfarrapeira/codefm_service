package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;

@Data
@Builder
@Generated
public class ExerciseStudentGrade {
    private Integer id;
    private Integer studentId;
    private Integer exerciseId;
    private Integer grade;
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
}

