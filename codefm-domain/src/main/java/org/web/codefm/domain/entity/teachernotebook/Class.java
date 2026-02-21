package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class Class {
    private Integer id;
    private Integer schoolId;
    private String name;
    private String schoolYear;
    private LocalDate deletionDate;
}
