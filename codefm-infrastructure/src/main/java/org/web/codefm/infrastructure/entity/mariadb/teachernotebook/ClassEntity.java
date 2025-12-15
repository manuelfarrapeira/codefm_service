package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "school_id")
    private Integer schoolId;

    @Column(length = 100)
    private String name;

    @Column(name = "school_year", length = 100)
    private String schoolYear;
}
