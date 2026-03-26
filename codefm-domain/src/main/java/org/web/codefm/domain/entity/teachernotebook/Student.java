package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
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
    private String shape;
    private LocalDate deletionDate;
    private List<Integer> classIds;
}

