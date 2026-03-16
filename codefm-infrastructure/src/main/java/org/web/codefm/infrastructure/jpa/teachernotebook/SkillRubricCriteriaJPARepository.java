package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SkillRubricCriteriaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRubricCriteriaJPARepository extends JpaRepository<SkillRubricCriteriaEntity, Integer> {

    @Query("SELECT c FROM SkillRubricCriteriaEntity c WHERE c.rubricId = :rubricId AND c.deletionDate IS NULL ORDER BY c.gradeStart ASC")
    List<SkillRubricCriteriaEntity> findActiveByRubricId(@Param("rubricId") Integer rubricId);

    @Query("SELECT c FROM SkillRubricCriteriaEntity c WHERE c.id = :id AND c.deletionDate IS NULL")
    Optional<SkillRubricCriteriaEntity> findActiveById(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE SkillRubricCriteriaEntity c SET c.deletionDate = CURRENT_DATE WHERE c.id = :id AND c.deletionDate IS NULL")
    void softDeleteById(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE SkillRubricCriteriaEntity c SET c.deletionDate = CURRENT_DATE WHERE c.rubricId = :rubricId AND c.deletionDate IS NULL")
    void softDeleteByRubricId(@Param("rubricId") Integer rubricId);

    @Modifying
    @Query("UPDATE SkillRubricCriteriaEntity c SET c.deletionDate = CURRENT_DATE WHERE c.rubricId IN :rubricIds AND c.deletionDate IS NULL")
    void softDeleteByRubricIds(@Param("rubricIds") List<Integer> rubricIds);
}

