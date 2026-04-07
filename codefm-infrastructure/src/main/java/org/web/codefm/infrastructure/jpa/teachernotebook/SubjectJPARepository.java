package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectJPARepository extends JpaRepository<SubjectEntity, Integer> {

    @Query("SELECT s FROM SubjectEntity s WHERE s.teacherId = :teacherId AND s.deletionDate IS NULL ORDER BY s.name ASC")
    List<SubjectEntity> findByTeacherId(@Param("teacherId") Integer teacherId);

    @Query("SELECT s FROM SubjectEntity s WHERE s.id = :id AND s.deletionDate IS NULL")
    Optional<SubjectEntity> findByIdAndDeletionDateIsNull(@Param("id") Integer id);

    @Query("SELECT s FROM SubjectEntity s WHERE s.id = :id AND s.teacherId = :teacherId AND s.deletionDate IS NULL")
    Optional<SubjectEntity> findByIdAndTeacherIdAndDeletionDateIsNull(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    @Query("SELECT sc.classId FROM SubjectClassEntity sc WHERE sc.id = :subjectClassId")
    Optional<Integer> findDistinctClassIdBySubjectClassId(@Param("subjectClassId") Integer subjectClassId);
}
