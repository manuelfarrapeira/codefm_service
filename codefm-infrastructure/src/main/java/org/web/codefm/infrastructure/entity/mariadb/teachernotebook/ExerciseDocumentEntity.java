package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "class_subject_exercise_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ExerciseDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "class_subject_exercise", nullable = false)
    private Integer exerciseId;

    @Column(nullable = false, length = 255)
    private String document;

    @Column(columnDefinition = "TEXT")
    private String description;
}

