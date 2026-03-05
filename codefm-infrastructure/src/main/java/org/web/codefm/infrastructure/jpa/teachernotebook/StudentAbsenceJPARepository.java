package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentAbsenceEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAbsenceJPARepository extends JpaRepository<StudentAbsenceEntity, Integer> {

	List<StudentAbsenceEntity> findByStudentClassId(Integer studentClassId);

	List<StudentAbsenceEntity> findByStudentClassIdAndAbsenceDate(Integer studentClassId, LocalDate absenceDate);

	@Query("SELECT sa FROM StudentAbsenceEntity sa " + "JOIN StudentClassEntity sc ON sa.studentClassId = sc.id "
			+ "WHERE sc.classId = :classId AND sa.absenceDate = :absenceDate")
	List<StudentAbsenceEntity> findByClassIdAndAbsenceDate(@Param("classId") Integer classId,
			@Param("absenceDate") LocalDate absenceDate);

	@Query("SELECT sa FROM StudentAbsenceEntity sa " + "JOIN StudentClassEntity sc ON sa.studentClassId = sc.id "
			+ "JOIN ClassEntity c ON sc.classId = c.id " + "JOIN SchoolEntity s ON c.schoolId = s.id "
			+ "WHERE sa.id = :id AND s.teacherId = :teacherId")
	Optional<StudentAbsenceEntity> findByIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

	boolean existsByStudentClassIdAndSubjectIdAndAbsenceDate(Integer studentClassId, Integer subjectId,
			LocalDate absenceDate);

	@Modifying
	@Transactional
	void deleteByStudentClassIdAndAbsenceDate(Integer studentClassId, LocalDate absenceDate);
}
