package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassRubricEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRubricJPARepository extends JpaRepository<ClassRubricEntity, Integer> {

    @Query("SELECT cr FROM ClassRubricEntity cr WHERE cr.classId = :classId AND cr.deletionDate IS NULL")
    List<ClassRubricEntity> findByClassIdAndDeletionDateIsNull(@Param("classId") Integer classId);

    @Query("SELECT cr FROM ClassRubricEntity cr WHERE cr.id = :id AND cr.deletionDate IS NULL")
    Optional<ClassRubricEntity> findByIdAndDeletionDateIsNull(@Param("id") Integer id);

    @Query("SELECT cr FROM ClassRubricEntity cr " +
            "JOIN ClassEntity c ON cr.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE cr.id = :id AND s.teacherId = :teacherId AND cr.deletionDate IS NULL")
    Optional<ClassRubricEntity> findByIdAndTeacherIdAndDeletionDateIsNull(
            @Param("id") Integer id,
            @Param("teacherId") Integer teacherId);

    @Transactional
    @Modifying
    @Query("UPDATE ClassRubricEntity cr SET cr.deletionDate = CURRENT_DATE WHERE cr.id = :id AND cr.deletionDate IS NULL")
    void softDeleteById(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE ClassRubricEntity cr SET cr.deletionDate = CURRENT_DATE WHERE cr.classId = :classId AND cr.deletionDate IS NULL")
    void softDeleteByClassId(@Param("classId") Integer classId);

    @Transactional
    @Modifying
    @Query("UPDATE ClassRubricEntity cr SET cr.deletionDate = CURRENT_DATE WHERE cr.rubricId = :rubricId AND cr.deletionDate IS NULL")
    void softDeleteByRubricId(@Param("rubricId") Integer rubricId);

    @Query("SELECT cr.id FROM ClassRubricEntity cr WHERE cr.classId = :classId AND cr.deletionDate IS NULL")
    List<Integer> findActiveIdsByClassId(@Param("classId") Integer classId);

    @Query("SELECT cr.id FROM ClassRubricEntity cr WHERE cr.rubricId = :rubricId AND cr.deletionDate IS NULL")
    List<Integer> findActiveIdsByRubricId(@Param("rubricId") Integer rubricId);

    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM ClassRubricEntity cr " +
            "WHERE cr.classId = :classId AND cr.rubricId = :rubricId AND cr.deletionDate IS NULL")
    boolean existsByClassIdAndRubricIdAndDeletionDateIsNull(
            @Param("classId") Integer classId,
            @Param("rubricId") Integer rubricId);

    @Query("SELECT DISTINCT cr.classId FROM ClassRubricEntity cr WHERE cr.id IN :ids")
    List<Integer> findDistinctClassIdsByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT DISTINCT cr.classId FROM ClassRubricEntity cr WHERE cr.rubricId = :rubricId AND cr.deletionDate IS NULL")
    List<Integer> findDistinctClassIdsByRubricId(@Param("rubricId") Integer rubricId);

    @Query("SELECT DISTINCT cr.classId FROM ClassRubricEntity cr WHERE cr.rubricId IN :rubricIds AND cr.deletionDate IS NULL")
    List<Integer> findDistinctClassIdsByRubricIds(@Param("rubricIds") List<Integer> rubricIds);
}


