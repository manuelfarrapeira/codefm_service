package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class StudentGroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_group_id", nullable = false)
    private Integer studentGroupId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;
}
