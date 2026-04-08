package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.CalendarAlertEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarAlertJPARepository extends JpaRepository<CalendarAlertEntity, Integer> {

    List<CalendarAlertEntity> findByTeacherIdOrderByDateAsc(Integer teacherId);

    Optional<CalendarAlertEntity> findByIdAndTeacherId(Integer id, Integer teacherId);

    @Query("SELECT c FROM CalendarAlertEntity c WHERE c.teacherId = :teacherId AND YEAR(c.date) = :year AND MONTH(c.date) = :month ORDER BY c.date ASC")
    List<CalendarAlertEntity> findByTeacherIdAndYearAndMonth(@Param("teacherId") Integer teacherId, @Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT c FROM CalendarAlertEntity c WHERE c.teacherId = :teacherId AND YEAR(c.date) = :year AND MONTH(c.date) >= :startMonth AND MONTH(c.date) <= :endMonth ORDER BY c.date ASC")
    List<CalendarAlertEntity> findByTeacherIdAndYearAndMonthRange(@Param("teacherId") Integer teacherId, @Param("year") Integer year, @Param("startMonth") Integer startMonth, @Param("endMonth") Integer endMonth);
}

