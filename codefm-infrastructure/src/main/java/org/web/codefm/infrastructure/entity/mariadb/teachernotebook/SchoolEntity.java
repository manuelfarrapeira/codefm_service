package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SchoolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String town;

    @Column(nullable = false)
    private Integer tlf;

    @OneToMany
    @JoinColumn(name = "school_id")
    private List<ClassEntity> classes;
}
