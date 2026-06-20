package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "class_rubrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ClassRubricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_class", nullable = false)
    private Integer classId;

    @Column(name = "id_rubric", nullable = false)
    private Integer rubricId;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

