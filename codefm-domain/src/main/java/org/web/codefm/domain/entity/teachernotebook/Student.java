package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Generated
public class Student {
    private Integer id;
    private Integer teacherId;
    private String name;
    private String surnames;
    private LocalDate dateOfBirth;
    private String gender;
    private String additionalInfo;
    private String photo;
    private LocalDate deletionDate;
    private List<Integer> classIds;
}

