package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "skill_rubrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SkillRubricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "id_skill", nullable = false)
    private Integer skillId;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

