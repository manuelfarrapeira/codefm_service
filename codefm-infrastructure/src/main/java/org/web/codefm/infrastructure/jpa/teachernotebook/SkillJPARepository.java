package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillJPARepository extends JpaRepository<SkillEntity, Integer> {

    @Query("SELECT s FROM SkillEntity s WHERE s.teacherId = :teacherId AND s.deletionDate IS NULL ORDER BY s.title ASC")
    List<SkillEntity> findByTeacherId(@Param("teacherId") Integer teacherId);

    @Query("SELECT s FROM SkillEntity s WHERE s.id = :id AND s.deletionDate IS NULL")
    Optional<SkillEntity> findByIdAndDeletionDateIsNull(@Param("id") Integer id);

    @Query("SELECT s FROM SkillEntity s WHERE s.id = :id AND s.teacherId = :teacherId AND s.deletionDate IS NULL")
    Optional<SkillEntity> findByIdAndTeacherIdAndDeletionDateIsNull(@Param("id") Integer id, @Param("teacherId") Integer teacherId);
}

