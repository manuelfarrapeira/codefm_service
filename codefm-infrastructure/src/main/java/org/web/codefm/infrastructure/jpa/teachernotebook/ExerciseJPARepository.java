package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseJPARepository extends JpaRepository<ExerciseEntity, Integer> {

    List<ExerciseEntity> findBySubjectClassIdInAndDeletionDateIsNull(List<Integer> subjectClassIds);

    @Query("SELECT e FROM ExerciseEntity e " +
            "JOIN SubjectClassEntity sc ON e.subjectClassId = sc.id " +
            "JOIN ClassEntity c ON sc.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE e.id = :id AND s.teacherId = :teacherId AND e.deletionDate IS NULL")
    Optional<ExerciseEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SubjectClassEntity sc " +
            "JOIN ClassEntity c ON sc.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE sc.id = :subjectClassId AND s.teacherId = :teacherId AND sc.deletionDate IS NULL " +
            "AND c.deletionDate IS NULL AND s.deletionDate IS NULL")
    boolean subjectClassBelongsToTeacher(@Param("subjectClassId") Integer subjectClassId, @Param("teacherId") Integer teacherId);

    @Modifying
    @Transactional
    @Query("UPDATE ExerciseEntity e SET e.deletionDate = CURRENT_DATE WHERE e.id = :id")
    void softDeleteById(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE ExerciseEntity e SET e.deletionDate = CURRENT_DATE WHERE e.subjectClassId IN :subjectClassIds AND e.deletionDate IS NULL")
    void softDeleteBySubjectClassIds(@Param("subjectClassIds") List<Integer> subjectClassIds);

    @Query("SELECT e.id FROM ExerciseEntity e WHERE e.subjectClassId IN :subjectClassIds AND e.deletionDate IS NULL")
    List<Integer> findActiveIdsBySubjectClassIds(@Param("subjectClassIds") List<Integer> subjectClassIds);

    @Query("SELECT COALESCE(SUM(e.percentageGrade), 0) FROM ExerciseEntity e " +
            "WHERE e.subjectClassId = :subjectClassId AND e.quarter = :quarter AND e.deletionDate IS NULL")
    Integer sumPercentageGradeBySubjectClassIdAndQuarter(@Param("subjectClassId") Integer subjectClassId,
                                                         @Param("quarter") Integer quarter);

    @Query("SELECT COALESCE(SUM(e.percentageGrade), 0) FROM ExerciseEntity e " +
            "WHERE e.subjectClassId = :subjectClassId AND e.quarter = :quarter AND e.id != :excludeId AND e.deletionDate IS NULL")
    Integer sumPercentageGradeBySubjectClassIdAndQuarterExcludingId(@Param("subjectClassId") Integer subjectClassId,
                                                                    @Param("quarter") Integer quarter,
                                                                    @Param("excludeId") Integer excludeId);
}
