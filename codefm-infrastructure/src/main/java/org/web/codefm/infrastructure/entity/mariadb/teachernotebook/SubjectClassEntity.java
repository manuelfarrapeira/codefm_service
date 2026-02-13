package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "subjerct_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SubjectClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_subject", nullable = false)
    private Integer subjectId;

    @Column(name = "id_class", nullable = false)
    private Integer classId;

    @Column(name = "deletion_date")
    private LocalDate deletionDate;
}

