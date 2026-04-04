package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "group_assignment_grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class GroupAssignmentGradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_assignment_id", nullable = false)
    private Integer groupAssignmentId;

    @Column(name = "group_id", nullable = false)
    private Integer groupId;

    @Column(nullable = false)
    private Double grade;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

