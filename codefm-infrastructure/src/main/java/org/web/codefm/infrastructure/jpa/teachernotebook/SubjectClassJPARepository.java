package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectClassJPARepository extends JpaRepository<SubjectClassEntity, Integer> {

    @Query("SELECT sc FROM SubjectClassEntity sc WHERE sc.classId = :classId AND sc.deletionDate IS NULL")
    List<SubjectClassEntity> findByClassIdAndDeletionDateIsNull(@Param("classId") Integer classId);

    @Query("SELECT sc FROM SubjectClassEntity sc WHERE sc.subjectId = :subjectId AND sc.classId = :classId AND sc.deletionDate IS NULL")
    Optional<SubjectClassEntity> findBySubjectIdAndClassIdAndDeletionDateIsNull(@Param("subjectId") Integer subjectId, @Param("classId") Integer classId);

    @Modifying
    @Transactional
    @Query("UPDATE SubjectClassEntity sc SET sc.deletionDate = CURRENT_DATE WHERE sc.classId = :classId AND sc.subjectId IN :subjectIds AND sc.deletionDate IS NULL")
    void softDeleteByClassIdAndSubjectIds(@Param("classId") Integer classId, @Param("subjectIds") List<Integer> subjectIds);

    @Query("SELECT DISTINCT sc.classId FROM SubjectClassEntity sc " +
            "JOIN ClassEntity c ON sc.classId = c.id " +
            "JOIN SchoolEntity s ON c.schoolId = s.id " +
            "WHERE s.teacherId = :teacherId AND sc.deletionDate IS NULL AND c.deletionDate IS NULL AND s.deletionDate IS NULL")
    List<Integer> findClassIdsByTeacherId(@Param("teacherId") Integer teacherId);

    @Modifying
    @Transactional
    @Query("UPDATE SubjectClassEntity sc SET sc.deletionDate = CURRENT_DATE WHERE sc.classId = :classId AND sc.deletionDate IS NULL")
    void softDeleteByClassId(@Param("classId") Integer classId);

    @Modifying
    @Transactional
    @Query("UPDATE SubjectClassEntity sc SET sc.deletionDate = CURRENT_DATE WHERE sc.subjectId = :subjectId AND sc.deletionDate IS NULL")
    void softDeleteBySubjectId(@Param("subjectId") Integer subjectId);

    @Query("SELECT sc.id FROM SubjectClassEntity sc WHERE sc.classId = :classId AND sc.deletionDate IS NULL")
    List<Integer> findIdsByClassIdAndDeletionDateIsNull(@Param("classId") Integer classId);

    @Query("SELECT sc.id FROM SubjectClassEntity sc WHERE sc.subjectId = :subjectId AND sc.deletionDate IS NULL")
    List<Integer> findIdsBySubjectIdAndDeletionDateIsNull(@Param("subjectId") Integer subjectId);

    @Query("SELECT DISTINCT sc.classId FROM SubjectClassEntity sc WHERE sc.id IN :ids")
    List<Integer> findDistinctClassIdsBySubjectClassIds(@Param("ids") List<Integer> ids);

    @Query("SELECT DISTINCT sc.classId FROM SubjectClassEntity sc WHERE sc.subjectId = :subjectId AND sc.deletionDate IS NULL")
    List<Integer> findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(@Param("subjectId") Integer subjectId);
}
