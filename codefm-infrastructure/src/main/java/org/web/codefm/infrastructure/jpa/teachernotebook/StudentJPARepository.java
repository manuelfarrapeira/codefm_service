package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentJPARepository extends JpaRepository<StudentEntity, Integer> {

    Optional<StudentEntity> findByIdAndTeacherIdAndDeletionDateIsNull(Integer id, Integer teacherId);

    @Query("SELECT s FROM StudentEntity s WHERE s.teacherId = :teacherId AND s.deletionDate IS NULL " +
            "AND (:id IS NULL OR s.id = :id) " +
            "AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:surnames IS NULL OR LOWER(s.surnames) LIKE LOWER(CONCAT('%', :surnames, '%')))" +
            "ORDER BY s.name ASC")
    List<StudentEntity> searchStudents(@Param("teacherId") Integer teacherId,
                                       @Param("id") Integer id,
                                       @Param("name") String name,
                                       @Param("surnames") String surnames);
}
