package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassRubricCriteriaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassRubricCriteriaJPARepository extends JpaRepository<StudentClassRubricCriteriaEntity, Integer> {

    @Query("SELECT sc FROM StudentClassRubricCriteriaEntity sc " +
            "JOIN ClassRubricEntity cr ON sc.classRubricId = cr.id " +
            "WHERE cr.classId = :classId AND sc.deletionDate IS NULL AND cr.deletionDate IS NULL")
    List<StudentClassRubricCriteriaEntity> findByClassIdAndDeletionDateIsNull(@Param("classId") Integer classId);

    @Query("SELECT sc FROM StudentClassRubricCriteriaEntity sc " +
            "JOIN ClassRubricEntity cr ON sc.classRubricId = cr.id " +
            "WHERE cr.classId = :classId AND sc.studentId = :studentId AND sc.deletionDate IS NULL AND cr.deletionDate IS NULL")
    List<StudentClassRubricCriteriaEntity> findByClassIdAndStudentIdAndDeletionDateIsNull(
            @Param("classId") Integer classId,
            @Param("studentId") Integer studentId);

    @Query("SELECT sc FROM StudentClassRubricCriteriaEntity sc " +
            "JOIN ClassRubricEntity cr ON sc.classRubricId = cr.id " +
            "JOIN ClassEntity c ON cr.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE sc.id = :id AND s.teacherId = :teacherId AND sc.deletionDate IS NULL")
    Optional<StudentClassRubricCriteriaEntity> findByIdAndTeacherIdAndDeletionDateIsNull(
            @Param("id") Integer id,
            @Param("teacherId") Integer teacherId);

    @Transactional
    @Modifying
    @Query("UPDATE StudentClassRubricCriteriaEntity sc SET sc.deletionDate = CURRENT_DATE " +
            "WHERE sc.id = :id AND sc.deletionDate IS NULL")
    void softDeleteById(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE StudentClassRubricCriteriaEntity sc SET sc.deletionDate = CURRENT_DATE " +
            "WHERE sc.classRubricId = :classRubricId AND sc.deletionDate IS NULL")
    void softDeleteByClassRubricId(@Param("classRubricId") Integer classRubricId);

    @Transactional
    @Modifying
    @Query("UPDATE StudentClassRubricCriteriaEntity sc SET sc.deletionDate = CURRENT_DATE " +
            "WHERE sc.classRubricId IN :classRubricIds AND sc.deletionDate IS NULL")
    void softDeleteByClassRubricIds(@Param("classRubricIds") List<Integer> classRubricIds);

    @Transactional
    @Modifying
    @Query("UPDATE StudentClassRubricCriteriaEntity sc SET sc.deletionDate = CURRENT_DATE " +
            "WHERE sc.criterionId = :criterionId AND sc.deletionDate IS NULL")
    void softDeleteByCriterionId(@Param("criterionId") Integer criterionId);

    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM StudentClassRubricCriteriaEntity sc " +
            "WHERE sc.classRubricId = :classRubricId AND sc.studentId = :studentId AND sc.deletionDate IS NULL")
    boolean existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(
            @Param("classRubricId") Integer classRubricId,
            @Param("studentId") Integer studentId);
}


