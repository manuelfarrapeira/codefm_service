package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentGradeEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseStudentGradeJPARepository extends JpaRepository<ExerciseStudentGradeEntity, Integer> {

    List<ExerciseStudentGradeEntity> findByExerciseIdInAndDeletionDateIsNull(List<Integer> exerciseIds);

    List<ExerciseStudentGradeEntity> findByExerciseIdInAndStudentIdAndDeletionDateIsNull(List<Integer> exerciseIds, Integer studentId);

    @Query("SELECT g FROM ExerciseStudentGradeEntity g " +
            "JOIN ExerciseEntity e ON g.exerciseId = e.id " +
            "JOIN SubjectClassEntity sc ON e.subjectClassId = sc.id " +
            "JOIN ClassEntity c ON sc.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE g.id = :id AND s.teacherId = :teacherId AND g.deletionDate IS NULL")
    Optional<ExerciseStudentGradeEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    boolean existsByStudentIdAndExerciseIdAndDeletionDateIsNull(Integer studentId, Integer exerciseId);

    @Modifying
    @Transactional
    @Query("UPDATE ExerciseStudentGradeEntity g SET g.deletionDate = CURRENT_DATE WHERE g.id = :id")
    void softDeleteById(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE ExerciseStudentGradeEntity g SET g.deletionDate = CURRENT_DATE WHERE g.exerciseId IN :exerciseIds AND g.deletionDate IS NULL")
    void softDeleteByExerciseIds(@Param("exerciseIds") List<Integer> exerciseIds);
}

