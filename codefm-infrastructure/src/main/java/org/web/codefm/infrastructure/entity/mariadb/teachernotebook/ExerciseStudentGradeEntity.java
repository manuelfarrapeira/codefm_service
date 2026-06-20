package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "class_subject_exercise_student")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ExerciseStudentGradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_student", nullable = false)
    private Integer studentId;

    @Column(name = "id_exercise", nullable = false)
    private Integer exerciseId;

    @Column(nullable = false)
    private Double grade;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

