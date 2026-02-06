package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ScheduleEntity;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleJPARepository extends JpaRepository<ScheduleEntity, Integer> {

    List<ScheduleEntity> findByClassIdAndDeletionDateIsNullOrderByDayAscStartAsc(Integer classId);

    Optional<ScheduleEntity> findByIdAndDeletionDateIsNull(Integer id);

    @Query("SELECT s FROM ScheduleEntity s " +
            "JOIN ClassEntity c ON s.classId = c.id " +
            "JOIN SchoolEntity sc ON c.schoolId = sc.id " +
            "WHERE s.id = :id AND sc.teacherId = :teacherId AND s.deletionDate IS NULL")
    Optional<ScheduleEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    @Query("SELECT COUNT(s) FROM ScheduleEntity s " +
            "JOIN ClassEntity c ON s.classId = c.id " +
            "JOIN SchoolEntity sc ON c.schoolId = sc.id " +
            "WHERE s.id IN :ids AND sc.teacherId = :teacherId AND s.deletionDate IS NULL")
    long countByIdsAndTeacherId(@Param("ids") List<Integer> ids, @Param("teacherId") Integer teacherId);

    @Modifying
    @Transactional
    @Query("UPDATE ScheduleEntity s SET s.deletionDate = CURRENT_DATE WHERE s.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ScheduleEntity s " +
            "WHERE s.classId = :classId AND s.day = :day AND s.deletionDate IS NULL " +
            "AND ((s.start < :endTime AND s.end > :startTime))")
    boolean existsOverlappingSchedule(
            @Param("classId") Integer classId,
            @Param("day") Integer day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ScheduleEntity s " +
            "WHERE s.classId = :classId AND s.day = :day AND s.deletionDate IS NULL " +
            "AND s.id != :excludeId " +
            "AND ((s.start < :endTime AND s.end > :startTime))")
    boolean existsOverlappingScheduleExcluding(
            @Param("classId") Integer classId,
            @Param("day") Integer day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Integer excludeId);
}
