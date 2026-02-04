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
     * Finds a student by ID and teacher ID if not deleted.
     *
     * @param id        The unique identifier of the student
     * @param teacherId The unique identifier of the teacher
     * @return Optional containing the student if found and not deleted
     */
    Optional<Student> findByIdAndTeacherIdAndDeletionDateIsNull(Integer id, Integer teacherId);

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
     * @param id        The unique identifier of the student to delete
     * @param teacherId The unique identifier of the teacher
     * @return The deleted student
     */
    Student softDelete(Integer id, Integer teacherId);

    /**
     * Searches students by filters for a specific teacher. Excludes soft-deleted students.
     *
     * @param teacherId The unique identifier of the teacher
     * @param id        Student ID (optional)
     * @param name      Student name (optional, partial match)
     * @param surnames  Student surnames (optional, partial match)
     * @return List of students matching the filters
     */
    List<Student> searchStudents(Integer teacherId, Integer id, String name, String surnames);

    /**
     * Finds all students for a specific teacher. Excludes soft-deleted students.
     *
     * @param teacherId The unique identifier of the teacher
     * @return List of all students belonging to the teacher
     */
    List<Student> findAllByTeacherId(Integer teacherId);
}
