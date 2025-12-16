package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class School {
    private Integer id;
    private Integer teacherId;
    private String name;
    private String town;
    private Integer tlf;
    private List<Class> classes;
    private LocalDate deletionDate;
}
