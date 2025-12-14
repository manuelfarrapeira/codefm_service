package org.web.codefm.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class School {
    private Integer id;
    private Integer teacherId;
    private String name;
    private String town;
    private Integer tlf;
}
