package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseStudentDocumentEntity;

import java.util.List;

@Repository
public interface ExerciseStudentDocumentJPARepository extends JpaRepository<ExerciseStudentDocumentEntity, Integer> {

    List<ExerciseStudentDocumentEntity> findByGradeId(Integer gradeId);

    List<ExerciseStudentDocumentEntity> findByGradeIdIn(List<Integer> gradeIds);

    @Modifying
    @Transactional
    void deleteByGradeId(Integer gradeId);

    @Modifying
    @Transactional
    void deleteByGradeIdIn(List<Integer> gradeIds);

    @Query("SELECT d FROM ExerciseStudentDocumentEntity d " +
            "JOIN ExerciseStudentGradeEntity g ON d.gradeId = g.id " +
            "WHERE g.exerciseId = :exerciseId AND g.deletionDate IS NULL")
    List<ExerciseStudentDocumentEntity> findByExerciseId(@Param("exerciseId") Integer exerciseId);

    @Query("SELECT d FROM ExerciseStudentDocumentEntity d " +
            "JOIN ExerciseStudentGradeEntity g ON d.gradeId = g.id " +
            "WHERE g.exerciseId IN :exerciseIds AND g.deletionDate IS NULL")
    List<ExerciseStudentDocumentEntity> findByExerciseIdIn(@Param("exerciseIds") List<Integer> exerciseIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExerciseStudentDocumentEntity d WHERE d.gradeId IN " +
            "(SELECT g.id FROM ExerciseStudentGradeEntity g WHERE g.exerciseId = :exerciseId AND g.deletionDate IS NULL)")
    void deleteByExerciseId(@Param("exerciseId") Integer exerciseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExerciseStudentDocumentEntity d WHERE d.gradeId IN " +
            "(SELECT g.id FROM ExerciseStudentGradeEntity g WHERE g.exerciseId IN :exerciseIds AND g.deletionDate IS NULL)")
    void deleteByExerciseIdIn(@Param("exerciseIds") List<Integer> exerciseIds);

    @Query("SELECT d FROM ExerciseStudentDocumentEntity d " +
            "JOIN ExerciseStudentGradeEntity g ON d.gradeId = g.id " +
            "WHERE g.studentId = :studentId AND g.deletionDate IS NULL")
    List<ExerciseStudentDocumentEntity> findByStudentId(@Param("studentId") Integer studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExerciseStudentDocumentEntity d WHERE d.gradeId IN " +
            "(SELECT g.id FROM ExerciseStudentGradeEntity g WHERE g.studentId = :studentId AND g.deletionDate IS NULL)")
    void deleteByStudentId(@Param("studentId") Integer studentId);

    @Query("SELECT d FROM ExerciseStudentDocumentEntity d " +
            "JOIN ExerciseStudentGradeEntity g ON d.gradeId = g.id " +
            "WHERE g.studentId = :studentId AND g.deletionDate IS NULL " +
            "AND g.exerciseId IN (SELECT e.id FROM ExerciseEntity e " +
            "JOIN SubjectClassEntity sc ON e.subjectClassId = sc.id " +
            "WHERE sc.classId = :classId AND e.deletionDate IS NULL)")
    List<ExerciseStudentDocumentEntity> findByStudentIdAndClassId(@Param("studentId") Integer studentId, @Param("classId") Integer classId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExerciseStudentDocumentEntity d WHERE d.gradeId IN " +
            "(SELECT g.id FROM ExerciseStudentGradeEntity g " +
            "WHERE g.studentId = :studentId AND g.deletionDate IS NULL " +
            "AND g.exerciseId IN (SELECT e.id FROM ExerciseEntity e " +
            "JOIN SubjectClassEntity sc ON e.subjectClassId = sc.id " +
            "WHERE sc.classId = :classId AND e.deletionDate IS NULL))")
    void deleteByStudentIdAndClassId(@Param("studentId") Integer studentId, @Param("classId") Integer classId);
}
