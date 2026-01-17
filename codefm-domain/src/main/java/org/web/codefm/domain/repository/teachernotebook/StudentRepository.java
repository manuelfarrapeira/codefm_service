package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for student data access operations.
 * Provides methods to retrieve and manage student information.
 */
public interface StudentRepository {

    /**
     * Saves a new student.
     *
     * @param student The student to save
     * @return The saved student with generated ID
     */
    Student save(Student student);

    /**
     * Finds a student by ID if not deleted.
     *
     * @param id The unique identifier of the student
     * @return Optional containing the student if found and not deleted
     */
    Optional<Student> findByIdAndDeletionDateIsNull(Integer id);

    /**
     * Updates an existing student.
     *
     * @param student The student to update
     * @return The updated student
     */
    Student update(Student student);

    /**
     * Performs soft delete on a student.
     *
     * @param id The unique identifier of the student to delete
     * @return The deleted student
     */
    Student softDelete(Integer id);

    /**
     * Searches students by filters. Excludes soft-deleted students.
     *
     * @param id       Student ID (optional)
     * @param name     Student name (optional, partial match)
     * @param surnames Student surnames (optional, partial match)
     * @return List of students matching the filters
     */
    List<Student> searchStudents(Integer id, String name, String surnames);
}
