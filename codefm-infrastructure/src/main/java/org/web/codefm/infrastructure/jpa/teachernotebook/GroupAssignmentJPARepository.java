package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupAssignmentJPARepository extends JpaRepository<GroupAssignmentEntity, Integer> {

    List<GroupAssignmentEntity> findByClassIdAndDeletionDateIsNull(Integer classId);

    @Query("SELECT ga FROM GroupAssignmentEntity ga " +
            "JOIN ClassEntity c ON ga.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE ga.id = :id AND s.teacherId = :teacherId AND ga.deletionDate IS NULL")
    Optional<GroupAssignmentEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentEntity ga SET ga.deletionDate = CURRENT_DATE " +
            "WHERE ga.id = :id AND ga.deletionDate IS NULL")
    void softDeleteById(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE GroupAssignmentEntity ga SET ga.deletionDate = CURRENT_DATE " +
            "WHERE ga.classId = :classId AND ga.deletionDate IS NULL")
    void softDeleteByClassId(@Param("classId") Integer classId);

    @Query("SELECT ga.id FROM GroupAssignmentEntity ga " +
            "WHERE ga.classId = :classId AND ga.deletionDate IS NULL")
    List<Integer> findActiveIdsByClassId(@Param("classId") Integer classId);
}

