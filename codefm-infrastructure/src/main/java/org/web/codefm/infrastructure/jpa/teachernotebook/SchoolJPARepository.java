package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;

import java.util.List;

@Repository
public interface SchoolJPARepository extends JpaRepository<SchoolEntity, Integer> {

    List<SchoolEntity> findByTeacherId(Integer teacherId);
}
