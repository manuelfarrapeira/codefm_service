package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_assignment_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class GroupAssignmentDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_assignment_id", nullable = false)
    private Integer groupAssignmentId;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(nullable = false, length = 255)
    private String document;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "group_document", nullable = false)
    private Boolean groupDocument;
}

