package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolJPARepository extends JpaRepository<SchoolEntity, Integer> {

    @Query("SELECT s FROM SchoolEntity s LEFT JOIN FETCH s.classes WHERE s.teacherId = :teacherId AND s.deletionDate IS NULL")
    List<SchoolEntity> findByTeacherId(@Param("teacherId") Integer teacherId);

    @Query("SELECT s FROM SchoolEntity s LEFT JOIN FETCH s.classes WHERE s.id = :id AND s.teacherId = :teacherId AND s.deletionDate IS NULL")
    Optional<SchoolEntity> findByIdAndTeacherIdAndDeletionDateIsNull(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    @Query("SELECT s FROM SchoolEntity s LEFT JOIN FETCH s.classes WHERE s.id = :id AND s.deletionDate IS NULL")
    Optional<SchoolEntity> findByIdAndDeletionDateIsNull(@Param("id") Integer id);
}
