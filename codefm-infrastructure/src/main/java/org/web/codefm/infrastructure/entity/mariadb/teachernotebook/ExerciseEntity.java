package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "class_subject_exercise")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_subject_class", nullable = false)
    private Integer subjectClassId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer quarter;

    @Column(name = "percentage_grade", nullable = false)
    private Integer percentageGrade;

    @Column(name = "max_grade", nullable = false)
    private Integer maxGrade;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

