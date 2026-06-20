package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRubricJPARepository extends JpaRepository<SkillRubricEntity, Integer> {

    @Query("SELECT r FROM SkillRubricEntity r WHERE r.skillId = :skillId AND r.deletionDate IS NULL ORDER BY r.title ASC")
    List<SkillRubricEntity> findBySkillId(@Param("skillId") Integer skillId);

    @Query("SELECT r FROM SkillRubricEntity r WHERE r.id = :id AND r.deletionDate IS NULL")
    Optional<SkillRubricEntity> findByIdAndDeletionDateIsNull(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE SkillRubricEntity r SET r.deletionDate = CURRENT_DATE WHERE r.id = :id AND r.deletionDate IS NULL")
    void softDeleteById(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE SkillRubricEntity r SET r.deletionDate = CURRENT_DATE WHERE r.skillId = :skillId AND r.deletionDate IS NULL")
    void softDeleteBySkillId(@Param("skillId") Integer skillId);

    @Query("SELECT r.id FROM SkillRubricEntity r WHERE r.skillId = :skillId AND r.deletionDate IS NULL")
    List<Integer> findActiveIdsBySkillId(@Param("skillId") Integer skillId);
}

