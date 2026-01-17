package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;

@Data
@Builder
@Generated
public class Student {
    private Integer id;
    private String name;
    private String surnames;
    private LocalDate dateOfBirth;
    private String additionalInfo;
    private String photo;
    private LocalDate deletionDate;
}

