package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "student_class_rubric_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class StudentClassRubricCriteriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_class_rubric", nullable = false)
    private Integer classRubricId;

    @Column(name = "id_student", nullable = false)
    private Integer studentId;

    @Column(name = "id_criterion", nullable = false)
    private Integer criterionId;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

