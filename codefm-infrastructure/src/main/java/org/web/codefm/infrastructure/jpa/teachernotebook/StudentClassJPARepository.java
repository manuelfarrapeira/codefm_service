package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;

import java.util.Optional;

@Repository
public interface StudentClassJPARepository extends JpaRepository<StudentClassEntity, Integer> {
    Optional<StudentClassEntity> findByClassIdAndStudentId(Integer classId, Integer studentId);
}

