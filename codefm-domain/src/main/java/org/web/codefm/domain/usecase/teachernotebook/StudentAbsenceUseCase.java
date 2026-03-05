package org.web.codefm.domain.usecase.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface that defines student absence operations for teachers. Handles
 * attendance management including creation, retrieval, and deletion.
 */
public interface StudentAbsenceUseCase {

	/**
	 * Creates one or more absences for a student in a class. If subjectId is
	 * provided, creates a single absence for that subject. If subjectId is null,
	 * creates absences for all subjects scheduled on the given date's day of week.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @param studentId
	 *            The unique identifier of the student
	 * @param subjectId
	 *            The unique identifier of the subject (nullable)
	 * @param date
	 *            The date of the absence
	 * @return List of created student absences
	 */
	List<StudentAbsence> createAbsences(Integer classId, Integer studentId, Integer subjectId, LocalDate date);

	/**
	 * Retrieves absences filtered by class and at least one of studentId or date.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @param studentId
	 *            The unique identifier of the student (nullable)
	 * @param date
	 *            The date to filter by (nullable)
	 * @return List of student absences with enriched details
	 */
	List<StudentAbsence> getAbsences(Integer classId, Integer studentId, LocalDate date);

	/**
	 * Deletes a single absence by its ID.
	 *
	 * @param id
	 *            The unique identifier of the absence to delete
	 */
	void deleteAbsence(Integer id);

	/**
	 * Deletes all absences for a student in a class on a specific date.
	 *
	 * @param classId
	 *            The unique identifier of the class
	 * @param studentId
	 *            The unique identifier of the student
	 * @param date
	 *            The date of the absences to delete
	 */
	void deleteAbsencesByStudentAndDate(Integer classId, Integer studentId, LocalDate date);
}
