package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "students_classes", schema = "teacher_notebook_pre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class StudentClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_class", nullable = false)
    private Integer classId;

    @Column(name = "id_student", nullable = false)
    private Integer studentId;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

