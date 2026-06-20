package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentGradeEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupAssignmentGradeJPARepository extends JpaRepository<GroupAssignmentGradeEntity, Integer> {

    @Query("SELECT g FROM GroupAssignmentGradeEntity g " +
            "JOIN GroupAssignmentEntity ga ON g.groupAssignmentId = ga.id " +
            "JOIN StudentGroupEntity sg ON g.groupId = sg.id " +
            "WHERE g.groupAssignmentId = :assignmentId AND g.deletionDate IS NULL AND ga.deletionDate IS NULL AND sg.deletionDate IS NULL")
    List<GroupAssignmentGradeEntity> findByGroupAssignmentIdAndDeletionDateIsNull(@Param("assignmentId") Integer assignmentId);

    @Query("SELECT g FROM GroupAssignmentGradeEntity g " +
            "WHERE g.groupAssignmentId = :assignmentId AND g.groupId = :groupId AND g.deletionDate IS NULL")
    Optional<GroupAssignmentGradeEntity> findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull(
            @Param("assignmentId") Integer assignmentId, @Param("groupId") Integer groupId);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentGradeEntity g SET g.deletionDate = CURRENT_DATE " +
            "WHERE g.id = :id AND g.deletionDate IS NULL")
    void softDeleteById(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentGradeEntity g SET g.deletionDate = CURRENT_DATE " +
            "WHERE g.groupAssignmentId = :assignmentId AND g.deletionDate IS NULL")
    void softDeleteByGroupAssignmentId(@Param("assignmentId") Integer assignmentId);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentGradeEntity g SET g.deletionDate = CURRENT_DATE " +
            "WHERE g.groupAssignmentId IN :assignmentIds AND g.deletionDate IS NULL")
    void softDeleteByGroupAssignmentIds(@Param("assignmentIds") List<Integer> assignmentIds);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentGradeEntity g SET g.deletionDate = CURRENT_DATE " +
            "WHERE g.groupId = :groupId AND g.deletionDate IS NULL")
    void softDeleteByGroupId(@Param("groupId") Integer groupId);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentGradeEntity g SET g.deletionDate = CURRENT_DATE " +
            "WHERE g.groupId IN :groupIds AND g.deletionDate IS NULL")
    void softDeleteByGroupIds(@Param("groupIds") List<Integer> groupIds);
}

