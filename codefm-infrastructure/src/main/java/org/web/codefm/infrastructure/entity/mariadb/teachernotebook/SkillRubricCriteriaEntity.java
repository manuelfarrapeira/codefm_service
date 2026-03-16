package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "skill_rubric_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SkillRubricCriteriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "id_rubric", nullable = false)
    private Integer rubricId;

    @Column(name = "grade_start", nullable = false)
    private Integer gradeStart;

    @Column(name = "grade_end", nullable = false)
    private Integer gradeEnd;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

