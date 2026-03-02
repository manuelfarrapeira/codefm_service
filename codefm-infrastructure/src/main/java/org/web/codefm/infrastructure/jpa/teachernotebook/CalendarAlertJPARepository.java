package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.CalendarAlertEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarAlertJPARepository extends JpaRepository<CalendarAlertEntity, Integer> {

    List<CalendarAlertEntity> findByTeacherIdOrderByDateAsc(Integer teacherId);

    Optional<CalendarAlertEntity> findByIdAndTeacherId(Integer id, Integer teacherId);
}

