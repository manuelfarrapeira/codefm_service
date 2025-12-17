package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassJPARepository extends JpaRepository<ClassEntity, Integer> {

    @Query("SELECT c FROM ClassEntity c JOIN SchoolEntity s ON c.schoolId = s.id WHERE c.schoolId = :schoolId AND s.teacherId = :teacherId AND c.deletionDate IS NULL AND s.deletionDate IS NULL")
    List<ClassEntity> findActiveClassesBySchoolIdAndTeacherId(@Param("schoolId") Integer schoolId, @Param("teacherId") Integer teacherId);

    /**
     * Finds a class by its ID, excluding soft-deleted classes.
     *
     * @param classId The ID of the class to find
     * @return An Optional containing the ClassEntity if found and not deleted, empty otherwise
     */
    Optional<ClassEntity> findByIdAndDeletionDateIsNull(Integer classId);

    /**
     * Finds a class by its ID, validating that it belongs to a school owned by the specified teacher
     * and that neither the class nor the school have been soft-deleted.
     *
     * @param classId   The ID of the class to find
     * @param teacherId The ID of the teacher who should own the school
     * @return An Optional containing the ClassEntity if found and valid, empty otherwise
     */
    @Query("SELECT c FROM ClassEntity c JOIN SchoolEntity s ON c.schoolId = s.id WHERE c.id = :classId AND s.teacherId = :teacherId AND c.deletionDate IS NULL AND s.deletionDate IS NULL")
    Optional<ClassEntity> findByIdAndTeacherIdAndDeletionDateIsNull(@Param("classId") Integer classId, @Param("teacherId") Integer teacherId);

    /**
     * Soft-deletes a class by setting its deletion_date to the current date.
     * Only deletes if the class exists and belongs to a school owned by the specified teacher.
     *
     * @param classId   The ID of the class to soft-delete
     * @param teacherId The ID of the teacher who owns the school
     * @return The number of rows affected (1 if successful, 0 if not found or not owned)
     */
    @Modifying
    @Transactional
    @Query("UPDATE ClassEntity c SET c.deletionDate = CURRENT_DATE WHERE c.id = :classId AND c.schoolId IN (SELECT s.id FROM SchoolEntity s WHERE s.teacherId = :teacherId AND s.deletionDate IS NULL)")
    int softDeleteClass(@Param("classId") Integer classId, @Param("teacherId") Integer teacherId);
}

