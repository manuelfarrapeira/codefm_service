package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentDocumentEntity;

import java.util.List;

@Repository
public interface GroupAssignmentDocumentJPARepository extends JpaRepository<GroupAssignmentDocumentEntity, Integer> {

    List<GroupAssignmentDocumentEntity> findByGroupAssignmentId(Integer groupAssignmentId);

    List<GroupAssignmentDocumentEntity> findByGroupAssignmentIdAndGroupId(Integer groupAssignmentId, Integer groupId);

    List<GroupAssignmentDocumentEntity> findByGroupAssignmentIdIn(List<Integer> groupAssignmentIds);

    List<GroupAssignmentDocumentEntity> findByGroupIdIn(List<Integer> groupIds);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupAssignmentDocumentEntity d WHERE d.groupAssignmentId = :assignmentId")
    void deleteByGroupAssignmentId(@Param("assignmentId") Integer assignmentId);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupAssignmentDocumentEntity d WHERE d.groupAssignmentId IN :assignmentIds")
    void deleteByGroupAssignmentIds(@Param("assignmentIds") List<Integer> assignmentIds);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupAssignmentDocumentEntity d WHERE d.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Integer groupId);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupAssignmentDocumentEntity d WHERE d.groupId IN :groupIds")
    void deleteByGroupIds(@Param("groupIds") List<Integer> groupIds);
}

