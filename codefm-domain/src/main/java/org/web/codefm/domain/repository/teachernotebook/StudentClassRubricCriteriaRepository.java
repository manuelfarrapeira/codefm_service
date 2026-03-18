package org.web.codefm.domain.repository.teachernotebook;

import org.web.codefm.domain.entity.teachernotebook.StudentClassRubricCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for student class rubric criteria assignment data access operations.
 * Provides methods to manage the association between students and rubric criteria within a class.
 */
public interface StudentClassRubricCriteriaRepository {

    /**
     * Finds all active criterion assignments for all students in a class,
     * enriched with rubricId, student name, surnames, criterion description and grade range.
     *
     * @param classId The unique identifier of the class
     * @return List of active criterion assignments with display fields populated
     */
    List<StudentClassRubricCriteria> findByClassId(Integer classId);

    /**
     * Finds all active criterion assignments for a specific student in a class,
     * enriched with rubricId, student name, surnames, criterion description and grade range.
     *
     * @param classId   The unique identifier of the class
     * @param studentId The unique identifier of the student
     * @return List of active criterion assignments for the student in the class
     */
    List<StudentClassRubricCriteria> findByClassIdAndStudentId(Integer classId, Integer studentId);

    /**
     * Finds an active criterion assignment by its ID, validating teacher ownership
     * through the class-rubric → class → school → teacher chain.
     *
     * @param id        The unique identifier of the criterion assignment
     * @param teacherId The unique identifier of the teacher
     * @return An Optional containing the assignment if found and owned by the teacher
     */
    Optional<StudentClassRubricCriteria> findByIdAndTeacherId(Integer id, Integer teacherId);

    /**
     * Saves a new student criterion assignment.
     *
     * @param criteria The StudentClassRubricCriteria object to save
     * @return The saved assignment with generated ID
     */
    StudentClassRubricCriteria save(StudentClassRubricCriteria criteria);

    /**
     * Soft-deletes a student criterion assignment by setting its deletion date.
     *
     * @param id The ID of the assignment to soft-delete
     */
    void softDeleteById(Integer id);

    /**
     * Soft-deletes all criterion assignments for a specific class-rubric assignment.
     * Used in cascade when a class-rubric assignment is soft-deleted.
     *
     * @param classRubricId The ID of the class-rubric assignment
     */
    void softDeleteByClassRubricId(Integer classRubricId);

    /**
     * Soft-deletes all criterion assignments for the given class-rubric assignment IDs.
     * Used in cascade when multiple class-rubric assignments are soft-deleted.
     *
     * @param classRubricIds The list of class-rubric assignment IDs
     */
    void softDeleteByClassRubricIds(List<Integer> classRubricIds);

    /**
     * Checks whether an active criterion assignment exists for the given class-rubric and student.
     *
     * @param classRubricId The unique identifier of the class-rubric assignment
     * @param studentId     The unique identifier of the student
     * @return true if an active assignment already exists, false otherwise
     */
    boolean existsByClassRubricIdAndStudentIdAndDeletionDateIsNull(Integer classRubricId, Integer studentId);
}

