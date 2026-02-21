package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassJPARepository extends JpaRepository<StudentClassEntity, Integer> {
    Optional<StudentClassEntity> findByClassIdAndStudentId(Integer classId, Integer studentId);

    @Query("SELECT sc.classId FROM StudentClassEntity sc WHERE sc.studentId = :studentId AND sc.deletionDate IS NULL")
    List<Integer> findClassIdsByStudentIdAndDeletionDateIsNull(@Param("studentId") Integer studentId);

    @Query("SELECT sc FROM StudentClassEntity sc " +
            "JOIN StudentEntity s ON sc.studentId = s.id " +
            "WHERE s.teacherId = :teacherId AND sc.deletionDate IS NULL AND s.deletionDate IS NULL")
    List<StudentClassEntity> findAllByTeacherIdAndDeletionDateIsNull(@Param("teacherId") Integer teacherId);

    @Modifying
    @Transactional
    @Query("UPDATE StudentClassEntity sc SET sc.deletionDate = CURRENT_DATE WHERE sc.classId = :classId AND sc.deletionDate IS NULL")
    void softDeleteByClassId(@Param("classId") Integer classId);

    @Modifying
    @Transactional
    @Query("UPDATE StudentClassEntity sc SET sc.deletionDate = CURRENT_DATE WHERE sc.studentId = :studentId AND sc.deletionDate IS NULL")
    void softDeleteByStudentId(@Param("studentId") Integer studentId);
}
