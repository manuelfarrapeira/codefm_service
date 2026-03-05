package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for student absence data access operations. Provides
 * methods to retrieve and manage student absence records.
 */
public interface StudentAbsenceRepository {

	/**
	 * Finds all absences for a specific student-class association.
	 *
	 * @param studentClassId
	 *            The unique identifier of the student-class association
	 * @return List of student absences with enriched student and subject details
	 */
	List<StudentAbsence> findByStudentClassId(Integer studentClassId);

	/**
	 * Finds all absences for a specific student-class association on a specific
	 * date.
	 *
	 * @param studentClassId
	 *            The unique identifier of the student-class association
	 * @param date
	 *            The date to filter by
	 * @return List of student absences with enriched student and subject details
	 */
	List<StudentAbsence> findByStudentClassIdAndDate(Integer studentClassId, LocalDate date);

	/**
	 * Finds all absences for a class on a specific date.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @param date
	 *            The date to filter by
	 * @return List of student absences with enriched student and subject details
	 */
	List<StudentAbsence> findByClassIdAndDate(Integer classId, LocalDate date);

	/**
	 * Finds an absence by its ID and validates teacher ownership.
	 *
	 * @param id
	 *            The unique identifier of the absence
	 * @param teacherId
	 *            The unique identifier of the teacher
	 * @return An Optional containing the absence if found and owned by the teacher
	 */
	Optional<StudentAbsence> findByIdAndTeacherId(Integer id, Integer teacherId);

	/**
	 * Saves multiple student absences.
	 *
	 * @param absences
	 *            The list of absences to save
	 * @return The list of saved absences with generated IDs
	 */
	List<StudentAbsence> saveAll(List<StudentAbsence> absences);

	/**
	 * Deletes a student absence by its ID (hard delete).
	 *
	 * @param id
	 *            The unique identifier of the absence to delete
	 */
	void deleteById(Integer id);

	/**
	 * Deletes all absences for a student-class association on a specific date (hard
	 * delete).
	 *
	 * @param studentClassId
	 *            The unique identifier of the student-class association
	 * @param date
	 *            The date of the absences to delete
	 */
	void deleteByStudentClassIdAndDate(Integer studentClassId, LocalDate date);

	/**
	 * Checks if an absence already exists for the given combination.
	 *
	 * @param studentClassId
	 *            The unique identifier of the student-class association
	 * @param subjectId
	 *            The unique identifier of the subject
	 * @param date
	 *            The date of the absence
	 * @return true if the absence already exists, false otherwise
	 */
	boolean existsByStudentClassIdAndSubjectIdAndDate(Integer studentClassId, Integer subjectId, LocalDate date);
}
