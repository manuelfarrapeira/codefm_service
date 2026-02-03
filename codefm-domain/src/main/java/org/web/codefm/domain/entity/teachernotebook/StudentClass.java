package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;

/**
 * Domain entity representing the association between a student and a class.
 */
@Data
@Builder
@Generated
public class StudentClass {
    private Integer id;
    private Integer classId;
    private Integer studentId;
    private LocalDate deletionDate;
}

