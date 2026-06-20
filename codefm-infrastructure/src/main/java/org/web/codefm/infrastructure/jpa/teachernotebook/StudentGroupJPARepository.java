package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupJPARepository extends JpaRepository<StudentGroupEntity, Integer> {

    List<StudentGroupEntity> findByClassIdAndDeletionDateIsNull(Integer classId);

    @Query("SELECT sg FROM StudentGroupEntity sg " +
            "JOIN ClassEntity c ON sg.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE sg.id = :groupId AND s.teacherId = :teacherId AND sg.deletionDate IS NULL")
    Optional<StudentGroupEntity> findByIdAndTeacherId(@Param("groupId") Integer groupId, @Param("teacherId") Integer teacherId);

    @Modifying
    @Transactional
    @Query("UPDATE StudentGroupEntity sg SET sg.deletionDate = CURRENT_DATE WHERE sg.id = :groupId AND sg.deletionDate IS NULL")
    void softDeleteById(@Param("groupId") Integer groupId);

    @Modifying
    @Transactional
    @Query("UPDATE StudentGroupEntity sg SET sg.deletionDate = CURRENT_DATE WHERE sg.classId = :classId AND sg.deletionDate IS NULL")
    void softDeleteByClassId(@Param("classId") Integer classId);

    @Modifying
    @Transactional
    @Query("UPDATE StudentGroupEntity sg SET sg.name = :name WHERE sg.id = :id")
    void updateNameById(@Param("id") Integer id, @Param("name") String name);

    @Query("SELECT sg.id FROM StudentGroupEntity sg WHERE sg.classId = :classId AND sg.deletionDate IS NULL")
    List<Integer> findActiveIdsByClassId(@Param("classId") Integer classId);
}
