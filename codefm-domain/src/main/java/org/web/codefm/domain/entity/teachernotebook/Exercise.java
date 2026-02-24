package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Generated
public class Exercise {
    private Integer id;
    private Integer subjectClassId;
    private Integer subjectId;
    private String subjectName;
    private String title;
    private String description;
    private Integer quarter;
    private Integer percentageGrade;
    private Integer maxGrade;
    private LocalDate deletionDate;
    private List<ExerciseDocument> documents;
}

