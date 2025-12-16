package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;

import java.util.List;

@Repository
public interface ClassJPARepository extends JpaRepository<ClassEntity, Integer> {

    @Query("SELECT c FROM ClassEntity c JOIN SchoolEntity s ON c.schoolId = s.id WHERE c.schoolId = :schoolId AND s.teacherId = :teacherId AND c.deletionDate IS NULL AND s.deletionDate IS NULL")
    List<ClassEntity> findActiveClassesBySchoolIdAndTeacherId(@Param("schoolId") Integer schoolId, @Param("teacherId") Integer teacherId);
}

