package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupMemberEntity;

import java.util.List;

@Repository
public interface StudentGroupMemberJPARepository extends JpaRepository<StudentGroupMemberEntity, Integer> {

    List<StudentGroupMemberEntity> findByStudentGroupIdIn(List<Integer> studentGroupIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentGroupMemberEntity m WHERE m.studentGroupId = :groupId")
    void deleteByStudentGroupId(@Param("groupId") Integer groupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentGroupMemberEntity m WHERE m.studentGroupId IN :groupIds")
    void deleteByStudentGroupIdIn(@Param("groupIds") List<Integer> groupIds);
}
